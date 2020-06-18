<template>
  <div class='login-wrapper'>
    <img class='logo' :src='logo' />
    <el-form
      v-if='!isRegister'
      ref='login'
      :rules='rules'
      :model='form'
      label-width='0'
      style='width:100%'
      @keydown.enter.native='handleEnter'
    >
      <el-form-item prop='userID'>
        <el-input v-model='form.userID' placeholder='请输入用户名' type='text' clearable></el-input>
      </el-form-item>
      <el-form-item prop='password'>
        <el-input
          v-model='form.password'
          placeholder='请输入密码'
          type='password'
          show-password
          clearable
        ></el-input>
      </el-form-item>
    </el-form>
    <el-form
            v-else
            ref='register_data'
            :rules='rules'
            :model='register_data'
            label-width='0'
            style='width:100%'
            @keydown.enter.native='handleEnter'
    >
      <el-form-item prop='userID'>
        <el-input v-model='register_data.userID' placeholder='请输入注册用户id' type='text' clearable></el-input>
      </el-form-item>
      <el-form-item prop='password'>
        <el-input
                v-model='register_data.password'
                placeholder='请输入注册密码'
                type='password'
                show-password
                clearable
        ></el-input>
      </el-form-item>
      <el-form-item prop='password_check'>
        <el-input
                v-model='register_data.password_check'
                placeholder='请再次输入注册密码'
                type='password'
                show-password
                clearable
        ></el-input>
      </el-form-item>
    </el-form>
    <el-button
      type='primary'
      @click='submit'
      style='width:100%; margin: 3px;'
      :loading='loading'
      v-show='!isRegister'
    >登录</el-button>
    <el-button
            @click='isRegister = true'
            style='width:100%; margin: 3px'
            v-show='!isRegister'
    >去注册</el-button>
    <el-button
            @click='isRegister = false'
            style='width:100%; margin: 3px;'
            v-show='isRegister'
    >去登录</el-button>
    <el-button
            type='primary'
            @click='register'
            style='width:100%; margin: 3px'
            :loading='loading'
            v-show='isRegister'
    >注册</el-button>
  </div>
</template>

<script>
import { Form, FormItem } from 'element-ui'
import logo from '../../assets/image/logo.png'
import axios from 'axios'
export default {

  name: 'Login',
  components: {
    ElForm: Form,
    ElFormItem: FormItem,
  },
  data() {
    const checkUserID = (rule, value, callback) => {
      if (!/^[a-zA-Z][a-zA-Z0-9_]{3,23}$/.test(value)) {
        callback(new Error('不合法（字母开头+字母/数字，长度4-24)'))
      } else {
        callback()
      }
    }
    const checkRoomNumber = (rule, value, callback) => {
      if (!/^[1-9][0-9]{0,9}$/.test(value)) {
        callback(new Error('不合法自然数，长度1-10'))
      } else {
        callback()
      }
    }
    const password_check = (rule, value, callback) => {
      if (value !== this.register_data.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    return {
      form: {
        userID: '',
        password: '',
        room: ''
      },
      register_data: {
        userID: '',
        password: '',
        password_check: '',
      },
      rules: {
        userID: [
          { required: true, message: '请输入 userID', trigger: 'blur' },
          { validator: checkUserID, trigger: 'blur' }
        ],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        password_check: [
          { required: true, message: '请再次输入密码', trigger: 'blur' },
          { validator: password_check, trigger: 'blur' }
        ],
        room: [
          { required: true, message: '请输入房间号', trigger: 'blur' },
          { validator: checkRoomNumber, trigger: 'blur' }
        ]
      },
      logo: logo,
      registerVisible: false,
      loading: false,
      isRegister: false
    }
  },
  methods: {
    handleEnter() {
      if (this.isRegister) {
        this.register()
      } else {
        this.submit()
      }
    },
    submit() {
      this.$refs['login'].validate(valid => {
        if (valid) {
          this.login()
        }
      })
    },
    register() {
      this.$refs['register_data'].validate(valid => {
        if (valid) {
          axios.post('http://47.103.30.166:8020/User/register', {
            accountUser: this.register_data.userID,
            passwordUser: this.register_data.password,
          }).then(res => {
            if (res.data == 'success') {
              this.$store.commit('showMessage', {
                type: 'success',
                message: '注册成功'
              })
              this.isRegister = true
            }
            if (res.data == 'account already exist') {
              this.$store.commit('showMessage', {
                type: 'error',
                message: '用户id "' + this.register_data.userID +'" 已存在'
              })
            }
          }).catch(err => {
            console.log(err)
          })
        }
      })
    },

    login() {
      this.loading = true
      axios
        .get('http://47.103.30.166:8020/User/SignIn', {
          params: {
            account: this.form.userID,
            password: this.form.password
          }
        })
        .then(res => {
          var signInRes = res.data
          if (signInRes === 'invalid account') {
            this.$store.commit('showMessage', {
              type: 'error',
              message: '帐号不存在'
            })
            this.loading = false
            return
          } else {
            if (signInRes === 'invalid password') {
              this.$store.commit('showMessage', {
                type: 'error',
                message: '密码不正确'
              })
              this.loading = false
              return
            } else {
              this.tim
                .login({
                  userID: this.form.userID,
                  userSig: window.genTestUserSig(this.form.userID).userSig
                })
                .then(() => {
                  this.loading = false
                  this.$store.commit('startComputeCurrent')
                  this.$store.commit({
                    type: 'GET_USER_INFO',
                    userID: this.form.userID,
                    userSig: window.genTestUserSig(this.form.userID).userSig,
                    sdkAppID: window.genTestUserSig('').SDKAppID
                  })
                  this.$store.commit('toggleIsLogin', true)
                  this.$store.commit('showMessage', {
                    type: 'success',
                    message: '登录成功'
                  })
                })
                .catch(error => {
                  this.loading = false
                  this.$store.commit('showMessage', {
                    message: '登录失败：' + error.message,
                    type: 'error'
                  })
                })
            }
          }
        })
    }
  }
}
</script>

<style lang='stylus' scoped>
.login-wrapper {
  display: flex
  align-items: center
  flex-direction: column
  width: 400px
  padding: 20px 80px 50px
  background: $white
  color: $black
  border-radius: 5px
  box-shadow: 0 11px 20px 0 rgba(0, 0, 0, 0.3)

  .logo {
    width: 130px
    height: 130px
  }

  .register-button {
    width: 100%
    margin: 6px 0 0 0
  }

  .user-selector {
    width: 100%
  }
}
</style>
