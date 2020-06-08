<template>
  <div class="login-wrapper">
    <img class="logo" :src="logo" />
    <el-form
      v-if="isLogin"
      ref="login"
      :rules="rules"
      :model="form"
      label-width="0"
      style="width:100%;"
      @keydown.enter.native="handleEnter"
    >
      <!-- Github登录方式 -->
      <!--el-form-item prop="userID" v-if="isLogin">
        <el-select v-model="form.userID" class="user-selector">
          <el-option
            v-for="index in 30"
            :key="index"
            :label="`user${index-1}`"
            :value="`user${index-1}`"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item prop="room" v-else>
        <el-input v-model="form.room" placeholder="请输入房间号" type="text" clearable></el-input>
      </el-form-item-->
      <!-- 线上版本登录方式 -->
      <el-form-item prop="userID">
        <el-input v-model="form.userID" placeholder="请输入用户名" type="text" clearable></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          placeholder="请输入密码"
          type="password"
          show-password
          clearable
        ></el-input>
      </el-form-item>
    </el-form>

    <el-form
      v-else
      ref="login"
      :rules="rules"
      :model="form"
      label-width="0"
      style="width:100%;"
      @keydown.enter.native="handleEnter"
    >
      <el-form-item prop="room">
        <el-input v-model="form.room" placeholder="请输入房间号" type="text" clearable></el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          placeholder="请输入密码"
          type="password"
          show-password
          clearable
        ></el-input>
      </el-form-item>
    </el-form>
    <el-button
      type="primary"
      @click="submit"
      style="width:100%; margin-top: 6px;"
      :loading="loading"
      v-if="isLogin"
    >登录</el-button>
    <el-button
      type="primary"
      @click="enterRoom"
      style="width:100%; margin-top: 6px;"
      :loading="loading"
      v-else
    >进入房间</el-button>
  </div>
</template>

<script>
import { Form, FormItem, Select, Option } from "element-ui";
import logo from "../../assets/image/logo.png";
import axios from "axios";
import { mapState } from "vuex";
export default {
  name: "Login",
  components: {
    ElForm: Form,
    ElFormItem: FormItem,
    ElSelect: Select,
    ElOption: Option
  },
  data() {
    const checkUserID = (rule, value, callback) => {
      if (!/^[a-zA-Z][a-zA-Z0-9_]{3,23}$/.test(value)) {
        callback(new Error("不合法（字母开头+字母/数字，长度4-24)"));
      } else {
        callback();
      }
    };
    const checkRoomNumber = (rule, value, callback) => {
      if (!/^[1-9][0-9]{0,9}$/.test(value)) {
        callback(new Error("不合法自然数，长度1-10"));
      } else {
        callback();
      }
    };
    return {
      form: {
        userID: "",
        password: "",
        room: ""
      },
      rules: {
        userID: [
          { required: true, message: "请输入 userID", trigger: "blur" },
          { validator: checkUserID, trigger: "blur" }
        ],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
        room: [
          { required: true, message: "请输入房间号", trigger: "blur" },
          { validator: checkRoomNumber, trigger: "blur" }
        ]
      },
      logo: logo,
      registerVisible: false,
      loading: false,
      isLogin: true
    };
  },
  methods: {
    handleEnter() {
      if (this.isLogin) {
        this.submit();
      } else {
        this.enterRoom();
      }
    },
    submit() {
      this.$refs["login"].validate(valid => {
        if (valid) {
          this.login();
        }
      });
    },
    enterRoomSuccess(groupID) {
      this.$store.dispatch("checkoutConversation", "GROUP" + groupID);
      this.$store.commit("toggleIsLogin", true);
      this.$store.commit("showMessage", {
        type: "success",
        message: "进入房间成功"
      });
    },
    enterRoom() {
      this.$refs["login"].validate(valid => {
        if (valid) {
          axios
            .get("http://47.103.30.166:8020/Room/SignIn", {
              params: {
                room_id: this.form.room,
                password: this.form.password
              }
            })
            .then(res => {
              var signInRes = res.data;
              if (signInRes === "invalid room") {
                this.$store.commit("showMessage", {
                  type: "error",
                  message: "房间不存在"
                });
                this.loading = false;
                return;
              } else {
                if (signInRes === "invalid password") {
                  this.$store.commit("showMessage", {
                    type: "error",
                    message: "密码不正确"
                  });
                  this.loading = false;
                  return;
                } else {
                  // get sdk room id from server
                  axios
                    .get("http://47.103.30.166:8020/Room/findById", {
                      params: {
                        room_id: this.form.room
                      }
                    })
                    .then(res => {
                      this.$store.commit("changeRoom", res.data);
                    });

                  this.tim.getGroupList().then(response => {
                    for (var group of response.data.groupList) {
                      if (group.groupID == this.form.room) {
                        this.enterRoomSuccess(group.groupID);
                        return;
                      }
                    }

                    this.tim
                      .joinGroup({
                        groupID: this.form.room
                      })
                      .then(response => {
                        this.enterRoomSuccess(response.data.group.groupID);
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
                            this.enterRoomSuccess(response.data.group.groupID);
                          })
                          .catch(error => {
                            this.$store.commit("showMessage", {
                              type: "error",
                              message: error
                            });
                          });
                      });
                  });
                }
              }
            });
        }
      });
    },
    login() {
      this.loading = true;
      axios
        .get("http://47.103.30.166:8020/User/SignIn", {
          params: {
            account: this.form.userID,
            password: this.form.password
          }
        })
        .then(res => {
          var signInRes = res.data;
          if (signInRes === "invalid account") {
            this.$store.commit("showMessage", {
              type: "error",
              message: "帐号不存在"
            });
            this.loading = false;
            return;
          } else {
            if (signInRes === "invalid password") {
              this.$store.commit("showMessage", {
                type: "error",
                message: "密码不正确"
              });
              this.loading = false;
              return;
            } else {
              this.tim
                .login({
                  userID: this.form.userID,
                  userSig: window.genTestUserSig(this.form.userID).userSig
                })
                .then(() => {
                  this.loading = false;
                  this.isLogin = false;
                  this.$store.commit("startComputeCurrent");
                  this.$store.commit({
                    type: "GET_USER_INFO",
                    userID: this.form.userID,
                    userSig: window.genTestUserSig(this.form.userID).userSig,
                    sdkAppID: window.genTestUserSig("").SDKAppID
                  });
                  this.$store.commit("showMessage", {
                    type: "success",
                    message: "登录成功"
                  });
                })
                .catch(error => {
                  this.loading = false;
                  this.$store.commit("showMessage", {
                    message: "登录失败：" + error.message,
                    type: "error"
                  });
                });
            }
          }
        });
    }
  }
};
</script>

<style lang="stylus" scoped>
.login-wrapper {
  display: flex;
  align-items: center;
  flex-direction: column;
  width: 400px;
  padding: 20px 80px 50px;
  background: $white;
  color: $black;
  border-radius: 5px;
  box-shadow: 0 11px 20px 0 rgba(0, 0, 0, 0.3);

  .logo {
    width: 130px;
    height: 130px;
  }

  .register-button {
    width: 100%;
    margin: 6px 0 0 0;
  }

  .user-selector {
    width: 100%;
  }
}
</style>
