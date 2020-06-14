<template>
  <div>
    <div class='login-wrapper'>
      <el-form
              ref="room"
              :rules='rules'
              :model='form'
              label-width='0'
              style='width:100%'
      >
        <el-form-item prop='room'>
          <el-input v-model='form.room' placeholder='请输入房间号' type='text' clearable></el-input>
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
      <el-button
              type='primary'
              @click='enterRoom'
              style='width:100%; margin-top: 6px'
              :loading='isLoading'
      >进入房间</el-button>
    </div>
  </div>
</template>

<script>
  import axios from 'axios'
  import { Form, FormItem } from 'element-ui'

  export default {
    name: 'Homepage',

    components: {
      ElForm: Form,
      ElFormItem: FormItem,
    },

    data() {
      const checkRoomNumber = (rule, value, callback) => {
        if (!/^[1-9][0-9]{0,9}$/.test(value)) {
          callback(new Error('不合法自然数，长度1-10'))
        } else {
          callback()
        }
      }

      return {
        form: {
          room: '',
          password: '',
        },
        rules: {
          password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
          room: [
            { required: true, message: '请输入房间号', trigger: 'blur' },
            { validator: checkRoomNumber, trigger: 'blur' }
          ]
        },
        isLoading: false,
      }
    },

    methods: {
      enterRoomSuccess(groupID) {
        this.$store.dispatch('checkoutConversation', 'GROUP' + groupID)
        this.$store.commit('showMessage', {
          type: 'success',
          message: '进入房间成功'
        })
        this.$emit('enterRoom')
      },
      enterRoom() {
        this.isLoading = true
        this.$refs['room'].validate(valid => {
          if (valid) {
            axios
              .get('http://47.103.30.166:8020/Room/SignIn', {
                params: {
                  room_id: this.form.room,
                  password: this.form.password
                }
              })
              .then(res => {
                var signInRes = res.data
                if (signInRes === 'invalid room') {
                  this.$store.commit('showMessage', {
                    type: 'error',
                    message: '房间不存在'
                  })
                  this.isLoading = false
                  return
                } else {
                  if (signInRes === 'invalid password') {
                    this.$store.commit('showMessage', {
                      type: 'error',
                      message: '密码不正确'
                    })
                    this.isLoading = false
                    return
                  } else {
                    // get sdk room id from server
                    axios
                      .get('http://47.103.30.166:8020/Room/findById', {
                        params: {
                          room_id: this.form.room
                        }
                      })
                      .then(res => {
                        this.$store.commit('changeRoom', res.data)
                      })

                    this.tim.getGroupList().then(response => {
                      for (var group of response.data.groupList) {
                        if (group.groupID == this.form.room) {
                          this.enterRoomSuccess(group.groupID)
                          return
                        }
                      }

                      this.tim
                        .joinGroup({
                          groupID: this.form.room
                        })
                        .then(response => {
                          this.enterRoomSuccess(response.data.group.groupID)
                        })
                        .catch(() => {
                          this.tim
                            .createGroup({
                              groupID: this.form.room,
                              name: this.form.room,
                              type: this.TIM.TYPES.GRP_PUBLIC,
                              joinOption: this.TIM.TYPES.JOIN_OPTIONS_FREE_ACCESS
                            })
                            .then(response => {
                              this.enterRoomSuccess(response.data.group.groupID)
                            })
                            .catch(error => {
                              this.$store.commit('showMessage', {
                                type: 'error',
                                message: error
                              })
                            })
                        })
                    })
                  }
                }
              })
          } else {
            this.isLoading = false
          }
        })
      },
    }
  }
</script>

<style lang='stylus' scoped>
  .login-wrapper {
    display: flex
    align-items: center
    flex-direction: column
    width: 400px
    padding: 60px 60px 60px
    background: $white
    color: $black
    border-radius: 5px
    box-shadow: 0 11px 20px 0 rgba(0, 0, 0, 0.3)
    margin-top: 30%

    .logo {
      width: 130px
      height: 130px
    }

    .user-selector {
      width: 100%
    }
  }
</style>
