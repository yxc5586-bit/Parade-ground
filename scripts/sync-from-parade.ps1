param(
    [string]$SourceRoot = (Join-Path $PSScriptRoot "..\..\Parade-ground"),
    [string]$MonorepoRoot = (Join-Path $PSScriptRoot ".."),
    [switch]$BackendOnly,
    [switch]$FrontendOnly,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Resolve-Directory {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $resolved = Resolve-Path -LiteralPath $Path -ErrorAction Stop
    if (-not (Test-Path -LiteralPath $resolved.Path -PathType Container)) {
        throw "$Name 不是有效目录：$Path"
    }
    return $resolved.Path
}

function Assert-ChildPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Parent,
        [Parameter(Mandatory = $true)]
        [string]$Child,
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    $parentFullPath = [System.IO.Path]::GetFullPath($Parent)
    $childFullPath = [System.IO.Path]::GetFullPath($Child)
    $comparison = [System.StringComparison]::OrdinalIgnoreCase
    if (-not $childFullPath.StartsWith($parentFullPath.TrimEnd([System.IO.Path]::DirectorySeparatorChar) + [System.IO.Path]::DirectorySeparatorChar, $comparison)) {
        throw "$Name 不在目标仓库内，已停止同步：$childFullPath"
    }
}

function Convert-ToRelativePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$BasePath,
        [Parameter(Mandatory = $true)]
        [string]$FullPath
    )

    return [System.IO.Path]::GetRelativePath($BasePath, $FullPath).Replace("\", "/")
}

function Test-SkippedPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RelativePath
    )

    $normalized = $RelativePath.Replace("\", "/")
    $parts = $normalized.Split("/", [System.StringSplitOptions]::RemoveEmptyEntries)
    $ignoredNames = @(
        ".git", ".idea", ".vscode", "node_modules", "dist", "target",
        "build", "logs", "coverage", ".DS_Store", "Thumbs.db"
    )

    foreach ($part in $parts) {
        if ($ignoredNames -contains $part) {
            return $true
        }
    }

    $fileName = [System.IO.Path]::GetFileName($normalized)
    if ($fileName -eq "HELP.md") {
        return $true
    }
    if ($fileName -eq ".env" -or $fileName.StartsWith(".env.")) {
        return $true
    }
    if ($fileName.EndsWith(".log") -or $fileName.EndsWith(".pid") -or $fileName.EndsWith(".pid.lock")) {
        return $true
    }

    return $false
}

function Get-GitVisibleFiles {
    param(
        [Parameter(Mandatory = $true)]
        [string]$RepoPath
    )

    $files = & git -C $RepoPath ls-files --cached --others --exclude-standard
    if ($LASTEXITCODE -ne 0) {
        throw "读取 Git 文件列表失败：$RepoPath"
    }

    return $files | Where-Object {
        $_ -and -not (Test-SkippedPath $_)
    } | Sort-Object -Unique
}

function Remove-TargetFiles {
    param(
        [Parameter(Mandatory = $true)]
        [string]$MonorepoRoot,
        [Parameter(Mandatory = $true)]
        [string]$TargetPath,
        [Parameter(Mandatory = $true)]
        [string]$TargetPrefix
    )

    $relativeTargetPath = Convert-ToRelativePath $MonorepoRoot $TargetPath
    $files = & git -C $MonorepoRoot ls-files --cached --others --exclude-standard -- $relativeTargetPath
    if ($LASTEXITCODE -ne 0) {
        throw "读取目标文件列表失败：$TargetPath"
    }

    foreach ($repoRelativePath in $files) {
        $projectRelativePath = $repoRelativePath.Substring($TargetPrefix.Length).TrimStart("/", "\")
        if (-not $projectRelativePath -or (Test-SkippedPath $projectRelativePath)) {
            continue
        }

        $fullPath = Join-Path $MonorepoRoot $repoRelativePath
        Assert-ChildPath $TargetPath $fullPath "待删除文件"
        if ($DryRun) {
            Write-Host "[dry-run] remove $repoRelativePath"
        } elseif (Test-Path -LiteralPath $fullPath -PathType Leaf) {
            Remove-Item -LiteralPath $fullPath -Force
        }
    }
}

function Copy-SourceFiles {
    param(
        [Parameter(Mandatory = $true)]
        [string]$SourcePath,
        [Parameter(Mandatory = $true)]
        [string]$TargetPath,
        [Parameter(Mandatory = $true)]
        [string[]]$Files
    )

    foreach ($relativePath in $Files) {
        $sourceFile = Join-Path $SourcePath $relativePath
        $targetFile = Join-Path $TargetPath $relativePath
        Assert-ChildPath $SourcePath $sourceFile "源文件"
        Assert-ChildPath $TargetPath $targetFile "目标文件"

        if ($DryRun) {
            Write-Host "[dry-run] copy $relativePath"
            continue
        }

        $targetDirectory = Split-Path -Parent $targetFile
        if (-not (Test-Path -LiteralPath $targetDirectory -PathType Container)) {
            New-Item -ItemType Directory -Path $targetDirectory -Force | Out-Null
        }
        Copy-Item -LiteralPath $sourceFile -Destination $targetFile -Force
    }
}

function Sync-Project {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [string]$SourcePath,
        [Parameter(Mandatory = $true)]
        [string]$TargetPath,
        [Parameter(Mandatory = $true)]
        [string]$TargetPrefix,
        [Parameter(Mandatory = $true)]
        [string]$MonorepoRoot
    )

    Write-Host "同步 $Name：$SourcePath -> $TargetPath"
    $files = @(Get-GitVisibleFiles $SourcePath)
    Remove-TargetFiles $MonorepoRoot $TargetPath $TargetPrefix
    Copy-SourceFiles $SourcePath $TargetPath $files
    Write-Host "$Name 同步完成，文件数：$($files.Count)"
}

$monorepoRootPath = Resolve-Directory $MonorepoRoot "monorepo 根目录"
$sourceRootPath = Resolve-Directory $SourceRoot "主线项目根目录"

$backendSource = Resolve-Directory (Join-Path $sourceRootPath "Parade-ground-backend") "主线后端"
$frontendSource = Resolve-Directory (Join-Path $sourceRootPath "Parade-ground-Fontend") "主线前端"
$backendTarget = Resolve-Directory (Join-Path $monorepoRootPath "backend") "monorepo 后端"
$frontendTarget = Resolve-Directory (Join-Path $monorepoRootPath "frontend") "monorepo 前端"

Assert-ChildPath $monorepoRootPath $backendTarget "monorepo 后端"
Assert-ChildPath $monorepoRootPath $frontendTarget "monorepo 前端"

if (-not $FrontendOnly) {
    Sync-Project "backend" $backendSource $backendTarget "backend/" $monorepoRootPath
}

if (-not $BackendOnly) {
    Sync-Project "frontend" $frontendSource $frontendTarget "frontend/" $monorepoRootPath
}

Write-Host "同步结束。请检查 git diff 后再提交。"
