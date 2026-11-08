<template>
  <div class="login-page">
    <div class="brand">
      <div class="brand-inner">
        <div class="logo">
          <span class="mark">H</span>
          <span class="text">云筑 Hestia</span>
        </div>
        <h1>把社区运维<br />从「救火」变成「运营」</h1>
        <p class="sub">
          工单全生命周期、SLA 时效管控、智能派单、设备台账与巡检闭环，
          一套系统管住社区里的每一件小事。
        </p>
        <ul class="features">
          <li><i class="el-icon-check"></i> 7 态工单状态机，流转全程可审计</li>
          <li><i class="el-icon-check"></i> SLA 超时自动升级，告别口头催办</li>
          <li><i class="el-icon-check"></i> 技能 × 负载 × 间隔加权派单</li>
          <li><i class="el-icon-check"></i> 巡检异常一键转工单，闭环处理</li>
        </ul>
      </div>
    </div>

    <div class="form-side">
      <div class="form-card">
        <h2>登录运维管理端</h2>
        <p class="tip">使用员工账号登录，演示环境账号 admin / 123456</p>

        <el-form ref="form" :model="form" :rules="rules" @keyup.enter.native="submit">
          <el-form-item prop="username">
            <el-input v-model="form.username" prefix-icon="el-icon-user" placeholder="登录账号" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              prefix-icon="el-icon-lock"
              show-password
              placeholder="登录密码"
            />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="submit" @click="submit">
            登 录
          </el-button>
        </el-form>

        <div class="demo">
          <span>快速填充：</span>
          <el-button type="text" @click="fill('admin', '123456')">管家</el-button>
          <el-button type="text" @click="fill('lisi', '123456')">工程师</el-button>
          <el-button type="text" @click="fill('sunqi', '123456')">主管</el-button>
        </div>
      </div>
      <div class="copyright">云筑 Hestia · 智慧社区设施运维平台</div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    fill(username, password) {
      this.form.username = username
      this.form.password = password
    },
    submit() {
      this.$refs.form.validate(async valid => {
        if (!valid) return
        this.loading = true
        try {
          const res = await login(this.form)
          await this.$store.dispatch('user/login', res.data)
          this.$message.success('欢迎回来，' + res.data.name)
          this.$router.replace('/dashboard')
        } catch (e) {
          /* 拦截器已提示 */
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  height: 100%;
  display: flex;
  background: #f5f7fa;
}

.brand {
  flex: 1.2;
  background: linear-gradient(150deg, #16233f 0%, #1d3566 45%, #2b4f9e 100%);
  color: #fff;
  display: flex;
  align-items: center;
  padding: 0 8%;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    width: 520px;
    height: 520px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(80, 130, 255, 0.35), transparent 65%);
    right: -160px;
    bottom: -180px;
  }

  .brand-inner {
    position: relative;
    z-index: 1;
    max-width: 520px;
  }

  .logo {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 46px;

    .mark {
      width: 38px;
      height: 38px;
      border-radius: 10px;
      background: linear-gradient(135deg, #5d8bff, #3b6ef0);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 18px;
    }

    .text {
      font-size: 18px;
      font-weight: 600;
      letter-spacing: 1px;
    }
  }

  h1 {
    font-size: 38px;
    line-height: 1.35;
    margin: 0 0 22px;
    font-weight: 600;
    letter-spacing: 1px;
  }

  .sub {
    font-size: 14px;
    line-height: 1.9;
    color: rgba(255, 255, 255, 0.72);
    margin: 0 0 36px;
  }

  .features {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      font-size: 13.5px;
      color: rgba(255, 255, 255, 0.85);
      margin-bottom: 14px;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        background: rgba(93, 139, 255, 0.25);
        color: #8fb2ff;
        font-size: 11px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.form-side {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
  position: relative;

  .form-card {
    width: 340px;
  }

  h2 {
    font-size: 24px;
    margin: 0 0 8px;
    color: #1f2733;
  }

  .tip {
    font-size: 12.5px;
    color: #9099a8;
    margin: 0 0 32px;
    line-height: 1.6;
  }

  ::v-deep .el-input__inner {
    height: 44px;
    line-height: 44px;
    border-radius: 8px;
  }

  .submit {
    width: 100%;
    height: 44px;
    border-radius: 8px;
    font-size: 15px;
    letter-spacing: 4px;
    margin-top: 6px;
    background: #3b6ef0;
    border-color: #3b6ef0;
  }

  .demo {
    margin-top: 26px;
    font-size: 12px;
    color: #9099a8;
    text-align: center;
  }

  .copyright {
    position: absolute;
    bottom: 26px;
    font-size: 11.5px;
    color: #b6bfcc;
  }
}

@media (max-width: 900px) {
  .brand {
    display: none;
  }
}
</style>
