# 练兵场直接部署模板

本目录保存腾讯云轻量应用服务器和 1Panel 场景下的部署模板。后端以 Spring Boot Fat JAR 运行，前端 dist 由 OpenResty 托管，不为业务应用构建 Docker 镜像。

## 固定路径

```text
/opt/resume-demo/parade/app/parade-ground.jar
/opt/resume-demo/parade/web/
/opt/resume-demo/parade/logs/
/opt/resume-demo/parade/bin/
/opt/resume-demo/config/parade.env
```

部署时把 `env/parade.env.example` 复制到服务器受限路径并填写真实值，权限建议设为 `root:parade 0640`。不要把真实环境文件提交到 Git。

安装顺序：

1. 确认 JAR 和前端 dist 来自同一次已验证构建。
2. 确认 MySQL `parade_ground` 和 Redis DB 1 可通过回环地址访问。
3. 将 `wait-for-health.sh` 放到 `/opt/resume-demo/parade/bin/` 并赋予执行权限。
4. 使用 `systemd-analyze verify` 检查 service，再执行 `daemon-reload`。
5. 在 1Panel 中应用 OpenResty 配置前先执行配置语法检查。
