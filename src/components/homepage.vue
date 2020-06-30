<template>
  <div class="container center">
    <div>
      <div class='login-wrapper'>
        <div class="title">
          {{ this.isEnterRoom ? "进入房间" : "创建房间" }}
        </div>
        <el-form
                ref="room"
                :rules='rules'
                :model='form'
                label-width='0'
                style='width:100%'
                v-if="isEnterRoom"
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
        <el-form
                ref="room"
                :rules='rules'
                :model='createRoomForm'
                label-width='75px'
                style='width:100%'
                v-else
        >
          <el-form-item label="房间名" prop="roomName">
            <el-input v-model="createRoomForm.roomName" placeholder='请输入房间名'></el-input>
          </el-form-item>
          <el-form-item label="是否公开">
            <el-switch v-model="createRoomForm.isPublic"></el-switch>
          </el-form-item>
          <el-form-item label="房间类型">
            <el-radio v-model="createRoomForm.roomKind" label="0">普通房间</el-radio>
            <el-radio v-model="createRoomForm.roomKind" label="1">电影房间(支持回放)</el-radio>
          </el-form-item>
          <el-form-item prop='password' label="密码" v-if="!createRoomForm.isPublic">
            <el-input
                    v-model='createRoomForm.password'
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
                style='width:100%; margin: 5px'
                :loading='isLoading'
                v-show='isEnterRoom'
        >进入房间</el-button>
        <el-button
                @click='isEnterRoom = false'
                style='width:100%; margin: 5px'
                v-show='isEnterRoom'
        >去创建房间</el-button>
        <el-button
                type='primary'
                @click='createRoom'
                style='width:100%; margin: 5px'
                :loading='isLoading'
                v-show='!isEnterRoom'
        >创建房间</el-button>
        <el-button
                @click='isEnterRoom = true'
                style='width:100%; margin: 5px'
                v-show='!isEnterRoom'
        >去进入房间</el-button>
      </div>
      <div class='login-wrapper' @click="showMyRoom">
        我的房间
      </div>
      <div class="profile">
        <my-profile/>
      </div>
    </div>
    <div v-if="showRooms" class="rooms center">
      <el-card class="box-card">
        <el-table
                :data="rooms"
                style="width: 100%">
          <el-table-column
                  prop="idRoom"
                  label="房间id"
                  width="180">
          </el-table-column>
          <el-table-column
                  prop="passwordRoom"
                  label="房间密码"
                  width="180">
          </el-table-column>
          <el-table-column
                  label="操作"
                  width="100">
            <template slot-scope="scope">
              <el-button @click="handleEnterRoom(scope.row)" type="text" size="small">进入房间</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script>
  import axios from 'axios'
  import { Form, FormItem, Switch, Radio, Table, TableColumn, Card} from 'element-ui'
  import MyProfile from './my-profile'
  import { mapState } from 'vuex'


  export default {
    name: 'Homepage',

    components: {
      ElForm: Form,
      ElFormItem: FormItem,
      ElSwitch: Switch,
      ElRadio: Radio,
      ElTable: Table,
      ElTableColumn: TableColumn,
      ElCard: Card,
      MyProfile,
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
        createRoomForm: {
          roomName: '',
          password: '',
          isPublic: true,
          roomKind:'0',
        },
        rules: {
          password: [{required: true, message: '请输入密码', trigger: 'blur' }],
          room: [
            { required: true, message: '请输入房间号', trigger: 'blur' },
            { validator: checkRoomNumber, trigger: 'blur' }
          ],
          roomName: [
            {required: true, message: '请输入房间名', trigger: 'blur' }
          ]
        },
        isLoading: false,
        isEnterRoom: true,
        showRooms: false,
        rooms: [],
      }
    },

    computed: {
      ...mapState({
        userID_cw: state => state.user.userID_cw
      }),
    },

    mounted() {
      axios.get('http://47.103.30.166:8020/User/findById', {
        params: {
          user_id: this.userID_cw
        }
      }).then(res => {
        this.rooms = res.data.rooms
      })
    },

    methods: {
      showMyRoom() {
        this.showRooms = !this.showRooms
      },
      handleEnterRoom(room) {
        axios
          .get('http://47.103.30.166:8020/Room/SignIn', {
            params: {
              room_id: room.idRoom,
              password: room.passwordRoom
            }
          })
          .then(res => {
            var signInRes = res.data
            if (signInRes === 'invalid room') {
              this.$store.commit('showMessage', {
                type: 'error',
                message: '房间不存在'
              })
              return
            } else {
              if (signInRes === 'invalid password') {
                this.$store.commit('showMessage', {
                  type: 'error',
                  message: '密码不正确'
                })
                return
              } else {
                // get room infor from backend
                axios
                  .get('http://47.103.30.166:8020/Room/findById', {
                    params: {
                      room_id: room.idRoom
                    }
                  })
                  .then(res => {
                    this.$store.commit('changeRoom', res.data)
                  })
                this.$store.commit('changeRoomId', room.idRoom)

                this.joinChatGroup(room.idRoom.toString())
              }
            }
          })
      },
      enterRoomSuccess(groupID) {
        this.$store.dispatch('checkoutConversation', 'GROUP' + groupID)
        if(this.$store.state.currentRoom.kindRoom==1) {
          this.$store.commit('showMessage',{
            type: 'success',
            message: '这是一个电影房间'
          })
        }else {
          this.$store.commit('showMessage',{
            type: 'success',
            message: '这是一个普通房间'
          })
        }
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
                    // get room infor from backend
                    axios
                      .get('http://47.103.30.166:8020/Room/findById', {
                        params: {
                          room_id: this.form.room
                        }
                      })
                      .then(res => {
                        this.$store.commit('changeRoom', res.data)
                      })
                      this.$store.commit('changeRoomId', this.form.room)

                    this.joinChatGroup(this.form.room)
                  }
                }
              })
          } else {
            this.isLoading = false
          }
        })
      },
      joinChatGroup(roomID, roomName='') {
        this.tim.getGroupList().then(response => {
          for (var group of response.data.groupList) {
            if (group.groupID == roomID) {
              this.enterRoomSuccess(group.groupID)
              return
            }
          }

          this.tim
            .joinGroup({
              groupID: roomID
            })
            .then(response => {
              this.enterRoomSuccess(response.data.group.groupID)
            })
            .catch(() => {
              this.tim
                .createGroup({
                  groupID: roomID,
                  name: roomName,
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
      },

      createRoom() {
        this.isLoading = true
        this.$refs['room'].validate(valid => {
          if (valid) {
            if (this.createRoomForm.roomKind=='0') {
            axios.get('http://47.103.30.166:8020/Room/new', {
              params: {
                user_id: this.userID_cw,
                passwordRoom: this.createRoomForm.password,
                statusRoom: this.createRoomForm.isPublic ? 'public' : 'private'
              }
            }).then(res => {
              window.console.log(res.data)
              if (res.data == 'invalid param') {
                this.$store.commit('showMessage', {
                  type: 'error',
                  message: '无效的创建参数'
                })
              }

              this.$store.commit('changeRoomId', res.data.toString())
              this.joinChatGroup(res.data.toString(), this.createRoomForm.roomName)
            }).catch(error => {
              window.console.log(error)
              this.$store.commit('showMessage', {
                type: 'error',
                message: error
              })
            })
            }else{
              axios.get('http://47.103.30.166:8020/Room/newMovieRoom', {
              params: {
                user_id: this.userID_cw,
                passwordRoom: this.createRoomForm.password,
                statusRoom: this.createRoomForm.isPublic ? 'public' : 'private'
              }
            }).then(res => {
              window.console.log(res.data)
              if (res.data == 'invalid param') {
                this.$store.commit('showMessage', {
                  type: 'error',
                  message: '无效的创建参数'
                })
              }

                this.$store.commit('changeRoomId', res.data.toString())
                this.joinChatGroup(res.data.toString(), this.createRoomForm.roomName)
            }).catch(error => {
              window.console.log(error)
              this.$store.commit('showMessage', {
                type: 'error',
                message: error
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
    padding: 35px 50px
    background: $white
    color: $black
    border-radius: 5px
    box-shadow: 0 11px 20px 0 rgba(0, 0, 0, 0.3)
    margin-top: 10%

    .logo {
      width: 130px
      height: 130px
    }

    .user-selector {
      width: 100%
    }
  }

  .title {
    padding-bottom: 30px;
  }

  .profile {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .rooms {
    display: flex;
    flex-direction column
    padding 80px
  }

  .container {
    display: flex;
    flex-direction row
  }

  .center {
    justify-content center
    align-content center
  }

</style>
