<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <div class="mark">H</div>
        <span v-show="!collapsed" class="name">云筑 Hestia</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        background-color="#1e2433"
        text-color="#a8b3c9"
        active-text-color="#ffffff"
        router
        class="menu"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="'/' + item.path">
          <i :class="item.meta.icon"></i>
          <span slot="title">{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="body">
      <el-header class="header" height="56px">
        <div class="left">
          <i
            :class="collapsed ? 'el-icon-s-unfold' : 'el-icon-s-fold'"
            class="toggle"
            @click="collapsed = !collapsed"
          ></i>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="right">
          <div class="notice" @click="openNotice">
            <el-badge :value="unread" :hidden="unread === 0" :max="99">
              <i class="el-icon-bell"></i>
            </el-badge>
          </div>

          <el-dropdown trigger="click" @command="onCommand">
            <span class="user">
              <span class="avatar">{{ (name || 'U').charAt(0) }}</span>
              <span class="uname">{{ name || '未登录' }}</span>
              <span class="role">{{ roleName }}</span>
              <i class="el-icon-arrow-down"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- 通知抽屉 -->
    <el-drawer title="站内通知" :visible.sync="noticeVisible" size="380px" @open="loadNotice">
      <div class="notice-list" v-loading="noticeLoading">
        <el-empty v-if="!notices.length" description="暂无通知" :image-size="80" />
        <div v-for="n in notices" :key="n.id" class="notice-item" :class="{ unread: !n.isRead }">
          <div class="n-title">{{ n.title }}</div>
          <div class="n-content">{{ n.content }}</div>
          <div class="n-time">{{ n.createTime }}</div>
        </div>
        <div v-if="notices.length" class="notice-foot">
          <el-button type="text" @click="markAll">全部标记已读</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- 修改密码 -->
    <el-dialog title="修改密码" :visible.sync="pwdVisible" width="420px">
      <el-form ref="pwdForm" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="pwdForm.confirm" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { readAllNotice } from '@/api/basic'
import request from '@/utils/request'

export default {
  name: 'Layout',
  data() {
    return {
      collapsed: false,
      unread: 0,
      noticeVisible: false,
      noticeLoading: false,
      notices: [],
      pwdVisible: false,
      pwdForm: { oldPassword: '', newPassword: '', confirm: '' },
      pwdRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [{ required: true, min: 6, message: '新密码至少 6 位', trigger: 'blur' }],
        confirm: [
          {
            validator: (rule, value, cb) => {
              if (value !== this.pwdForm.newPassword) cb(new Error('两次输入不一致'))
              else cb()
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  computed: {
    // user 模块带命名空间，需写完整 getter 名，否则取不到值（显示为“未登录”）
    ...mapGetters({ name: 'user/name', roleName: 'user/roleName' }),
    menus() {
      const root = this.$router.options.routes.find(r => r.path === '/')
      return (root.children || []).filter(c => !(c.meta && c.meta.hidden))
    },
    activeMenu() {
      return this.$route.path
    },
    currentTitle() {
      return this.$route.meta && this.$route.meta.title
    }
  },
  created() {
    this.loadUnread()
    this.timer = setInterval(this.loadUnread, 60000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  },
  methods: {
    async loadUnread() {
      try {
        const res = await request({ url: '/admin/notice/unread-count', method: 'get' })
        this.unread = res.data || 0
      } catch (e) {
        /* 忽略 */
      }
    },
    async openNotice() {
      this.noticeVisible = true
    },
    async loadNotice() {
      this.noticeLoading = true
      try {
        const res = await request({ url: '/admin/notice/list', method: 'get' })
        this.notices = res.data || []
      } finally {
        this.noticeLoading = false
      }
    },
    async markAll() {
      await readAllNotice()
      this.notices = this.notices.map(n => ({ ...n, isRead: 1 }))
      this.unread = 0
      this.$message.success('已全部标记为已读')
    },
    onCommand(cmd) {
      if (cmd === 'logout') {
        this.$confirm('确认退出登录？', '提示', { type: 'warning' })
          .then(() => {
            this.$store.dispatch('user/logout')
            this.$router.replace('/login')
          })
          .catch(() => {})
      } else if (cmd === 'password') {
        this.pwdForm = { oldPassword: '', newPassword: '', confirm: '' }
        this.pwdVisible = true
      }
    },
    submitPwd() {
      this.$refs.pwdForm.validate(async valid => {
        if (!valid) return
        await request({
          url: '/admin/auth/password',
          method: 'put',
          data: { oldPassword: this.pwdForm.oldPassword, newPassword: this.pwdForm.newPassword }
        })
        this.$message.success('密码修改成功，请重新登录')
        this.pwdVisible = false
        setTimeout(() => {
          this.$store.dispatch('user/logout')
          this.$router.replace('/login')
        }, 800)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.layout {
  height: 100%;
}

.aside {
  background: #1e2433;
  transition: width 0.2s;
  overflow-x: hidden;

  .logo {
    height: 56px;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 0 18px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);

    .mark {
      width: 28px;
      height: 28px;
      border-radius: 8px;
      background: linear-gradient(135deg, #4b7dff, #2f5be0);
      color: #fff;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .name {
      color: #fff;
      font-size: 15px;
      font-weight: 600;
      letter-spacing: 0.5px;
      white-space: nowrap;
    }
  }

  .menu {
    border-right: none;
  }

  ::v-deep .el-menu-item {
    height: 44px;
    line-height: 44px;
    margin: 2px 8px;
    border-radius: 6px;

    i {
      color: inherit;
      margin-right: 8px;
    }

    &:hover {
      background: #2a3347 !important;
    }

    &.is-active {
      background: linear-gradient(90deg, #3b6ef0, #2f5be0) !important;
    }
  }
}

.body {
  min-width: 0;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e6eaf0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .left {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  .toggle {
    font-size: 18px;
    cursor: pointer;
    color: #6b7789;

    &:hover {
      color: #3b6ef0;
    }
  }

  .right {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .notice {
    cursor: pointer;
    font-size: 17px;
    color: #6b7789;

    &:hover {
      color: #3b6ef0;
    }
  }

  .user {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;

    .avatar {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: #3b6ef0;
      color: #fff;
      font-size: 13px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .uname {
      font-size: 13px;
      color: #1f2733;
    }

    .role {
      font-size: 12px;
      color: #9099a8;
    }
  }
}

.main {
  background: #f5f7fa;
  padding: 0;
  overflow-y: auto;
}

.notice-list {
  padding: 0 20px;

  .notice-item {
    padding: 12px 0;
    border-bottom: 1px solid #eef1f6;

    &.unread .n-title::before {
      content: '';
      display: inline-block;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #f56c6c;
      margin-right: 6px;
      vertical-align: middle;
    }

    .n-title {
      font-size: 13px;
      font-weight: 600;
      color: #1f2733;
    }

    .n-content {
      font-size: 12px;
      color: #6b7789;
      margin-top: 4px;
      line-height: 1.6;
    }

    .n-time {
      font-size: 11px;
      color: #a8b3c9;
      margin-top: 4px;
    }
  }

  .notice-foot {
    text-align: center;
    padding: 12px 0;
  }
}
</style>
