<template>
  <div class="login-page">
    <section class="login-copy">
      <div class="brand-mark">PG</div>
      <h1>程序员技术练兵场</h1>
      <p>
        面向真实企业场景的方案设计闯关。AI 出题、AI 评审，薪资曲线会很诚实。
      </p>
      <div class="copy-grid">
        <span>业务场景</span>
        <span>架构选型</span>
        <span>方案复盘</span>
        <span>薪资成长</span>
      </div>
    </section>

    <section class="login-panel">
      <div class="panel-title">
        <h2>{{ isRegister ? '创建练兵账号' : '进入练兵场' }}</h2>
        <p>{{ isRegister ? '初始月薪固定 10000 元，之后全靠方案评审说话。' : '登录后继续你的闯关和薪资曲线。' }}</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model.trim="form.userAccount" placeholder="至少 4 位" autocomplete="username" />
        </el-form-item>
        <el-form-item v-if="isRegister" label="昵称" prop="userName">
          <el-input v-model.trim="form.userName" placeholder="例如 coder_player" />
        </el-form-item>
        <el-form-item label="密码" prop="userPassword">
          <el-input
            v-model="form.userPassword"
            type="password"
            show-password
            placeholder="至少 8 位"
            autocomplete="current-password"
          />
        </el-form-item>
        <el-form-item v-if="isRegister" label="确认密码" prop="checkPassword">
          <el-input v-model="form.checkPassword" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" :loading="submitting" @click="handleSubmit">
          {{ isRegister ? '注册并登录' : '登录' }}
        </el-button>
      </el-form>

      <button class="mode-btn" type="button" @click="toggleMode">
        {{ isRegister ? '已有账号，去登录' : '没有账号，注册一个' }}
      </button>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '../api/user'
import { useUser } from '../composables/useUser'

const router = useRouter()
const route = useRoute()
const { setUser } = useUser()

const isRegister = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  userAccount: '',
  userName: '',
  userPassword: '',
  checkPassword: '',
})

const rules = computed(() => ({
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号至少 4 位', trigger: 'blur' },
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  checkPassword: [
    { required: isRegister.value, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (!isRegister.value || value === form.userPassword) callback()
        else callback(new Error('两次输入的密码不一致'))
      },
      trigger: 'blur',
    },
  ],
}))

function toggleMode() {
  isRegister.value = !isRegister.value
  formRef.value?.clearValidate()
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isRegister.value) {
      await userApi.register({
        userAccount: form.userAccount,
        userPassword: form.userPassword,
        checkPassword: form.checkPassword,
        userName: form.userName,
      })
      ElMessage.success('注册成功，已自动登录')
    }

    const user = await userApi.login({
      userAccount: form.userAccount,
      userPassword: form.userPassword,
    })
    setUser(user)
    router.push(route.query.redirect || '/')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 30px;
  align-items: center;
  width: min(1120px, calc(100vw - 32px));
  min-height: 100vh;
  margin: 0 auto;
  padding: 34px 0;
}

.login-copy {
  padding: 36px;
}

.brand-mark {
  display: grid;
  width: 54px;
  height: 54px;
  place-items: center;
  color: #fff;
  font-weight: 900;
  background: var(--primary);
  border-radius: var(--radius);
}

h1 {
  max-width: 560px;
  margin: 22px 0 0;
  font-size: 46px;
  line-height: 1.1;
  letter-spacing: 0;
}

.login-copy p {
  max-width: 560px;
  margin: 18px 0 0;
  color: #35433a;
  font-size: 18px;
  line-height: 1.8;
}

.copy-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  max-width: 600px;
  margin-top: 28px;
}

.copy-grid span {
  padding: 12px;
  color: var(--primary-deep);
  background: var(--primary-soft);
  border: 1px solid #bfe5dc;
  border-radius: var(--radius);
  font-weight: 800;
  text-align: center;
}

.login-panel {
  padding: 26px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
}

.panel-title h2 {
  margin: 0;
  font-size: 24px;
}

.panel-title p {
  margin: 8px 0 22px;
  color: var(--muted);
  line-height: 1.6;
}

.submit-btn {
  width: 100%;
  min-height: 42px;
  margin-top: 6px;
}

.mode-btn {
  width: 100%;
  margin-top: 14px;
  padding: 0;
  color: var(--primary);
  background: transparent;
  border: 0;
  cursor: pointer;
  font-weight: 800;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    align-content: start;
    width: min(100vw - 20px, 620px);
  }

  .login-copy {
    padding: 12px 4px;
  }

  h1 {
    font-size: 34px;
  }

  .copy-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
