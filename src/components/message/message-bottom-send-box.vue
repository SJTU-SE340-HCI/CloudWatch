<template>
  <div id="message-send-box-wrapper" :style="focus ? {'backgroundColor': 'white'} : {}" @drop="dropHandler">
    <el-popover
      trigger="manual"
      v-model="showAtGroupMember"
      placement="top"
      style="max-height:500px;overflow-y:scroll;"
    >
      <el-radio-group
        v-model="atUserID"
        style="display:flex;flex-decoration: column;"
        v-for="member in memberList"
        :key="member.userID"
        @change="handleSelectAtUser"
      >
        <el-radio :label="member.userID">{{ member.nameCard || member.nick || member.userID }}</el-radio>
      </el-radio-group>
    </el-popover>
    <div class="bottom">
      <div class="voice">
        <VoiceCommunicate></VoiceCommunicate>
      </div>
      <el-popover placement="top" width="600" trigger="click">
        <div class="emojis">
          <div v-for="item in emojiName" class="emoji" :key="item" @click="chooseEmoji(item)">
            <img :src="emojiUrl + emojiMap[item]" style="width:30px;height:30px" />
          </div>
        </div>
        <i class="iconfont icon-smile emoji" slot="reference" title="发表情"></i>
      </el-popover>
      <textarea
        ref="text-input"
        rows="1"
        resize="false"
        v-model="messageContent"
        class="text-input"
        @focus="focus = true"
        @blur="focus = false"
        @keydown.enter.exact.prevent="handleEnter"
        @keyup.ctrl.enter.prevent.exact="handleLine"
        @keydown.shift.50.prevent.exact="handleAt"
        @keydown.up.stop="handleUp"
        @keydown.down.stop="handleDown"
      >
      </textarea>
      <el-tooltip
              class="item"
              effect="dark"
              content="按Enter发送消息，Ctrl+Enter换行"
              placement="left-start"
      >
        <div class="btn-send" @click="sendTextMessage">
          <div class="tim-icon-send"></div>
        </div>
      </el-tooltip>
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from 'vuex'
import {
  Form,
  FormItem,
  Input,
  Dialog,
  Popover,
  RadioGroup,
  Radio,
  Tooltip,
} from 'element-ui'
import { emojiMap, emojiName, emojiUrl } from '../../utils/emojiMap'
import VoiceCommunicate from '../voice'

export default {
  name: 'message-send-box',
  props: ['scrollMessageListToButtom'],
  components: {
    ElInput: Input,
    ElForm: Form,
    ElFormItem: FormItem,
    ElDialog: Dialog,
    ElPopover: Popover,
    ElRadioGroup: RadioGroup,
    ElRadio: Radio,
    ElTooltip: Tooltip,
    VoiceCommunicate: VoiceCommunicate,
  },
  data() {
    return {
      colors: ['#99A9BF', '#F7BA2A', '#FF9900'],
      messageContent: '',
      isSendCustomMessage: false,
      sendCustomDialogVisible: false,
      form: {
        data: '',
        description: '',
        extension: ''
      },
      file: '',
      emojiMap: emojiMap,
      emojiName: emojiName,
      emojiUrl: emojiUrl,
      showAtGroupMember: false,
      atUserID: '',
      focus: false
    }
  },
  computed: {
    ...mapGetters(['toAccount', 'currentConversationType']),
    ...mapState({
      memberList: state => state.group.currentMemberList,
      userID: state => state.user.userID,
      currentUserProfile: state => state.user.currentUserProfile,
    })
  },
  mounted() {
    this.$refs['text-input'].addEventListener('paste', this.handlePaste)
    this.$bus.$on('reEditMessage', this.reEditMessage)
  },
  beforeDestroy() {
    this.$refs['text-input'].removeEventListener('paste', this.handlePaste)
  },
  methods: {
    reEditMessage(payload) {
      this.messageContent = payload
    },
    handleSelectAtUser() {
      this.messageContent += this.atUserID + ' '
      this.showAtGroupMember = false
    },
    handleUp() {
      const index = this.memberList.findIndex(
        member => member.userID === this.atUserID
      )
      if (index - 1 < 0) {
        return
      }
      this.atUserID = this.memberList[index - 1].userID
    },
    handleDown() {
      const index = this.memberList.findIndex(
        member => member.userID === this.atUserID
      )
      if (index + 1 >= this.memberList.length) {
        return
      }
      this.atUserID = this.memberList[index + 1].userID
    },
    handleEnter() {
      if (this.showAtGroupMember) {
        this.handleSelectAtUser()
      } else {
        this.sendTextMessage()
      }
    },
    handleLine() {
      this.messageContent += '\n'
    },
    handleAt() {
      this.showAtGroupMember = true
      this.messageContent += '@'
    },
    handlePaste(e) {
      let clipboardData = e.clipboardData
      let file
      if (
        clipboardData &&
        clipboardData.files &&
        clipboardData.files.length > 0
      ) {
        file = clipboardData.files[0]
      }

      if (typeof file === 'undefined') {
        return
      }
      // 1. 创建消息实例，接口返回的实例可以上屏
      let message = this.tim.createImageMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: {
          file: file
        },
        onProgress: percent => {
          this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
        }
      })
      this.$store.commit('pushCurrentMessageList', message)

      // 2. 发送消息
      let promise = this.tim.sendMessage(message)
      promise.catch(error => {
        this.$store.commit('showMessage', {
          type: 'error',
          message: error.message
        })
      })
    },
    dropHandler(e) {
      e.preventDefault()
      let file = e.dataTransfer.files[0]
      let message = {}
      if (file.type === 'video/mp4') {
        message = this.tim.createVideoMessage({
          to: this.toAccount,
          conversationType: this.currentConversationType,
          payload: {
            file: file
          },
          onProgress: percent => {
            this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
          }
        })
      } else {
        message = this.tim.createFileMessage({
          to: this.toAccount,
          conversationType: this.currentConversationType,
          payload: {
            file: file
          },
          onProgress: percent => {
            this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
          }
        })
      }
      this.$store.commit('pushCurrentMessageList', message)
      this.tim
        .sendMessage(message)
        .then(() => {
          this.$refs.videoPicker.value = null
        })
        .catch(imError => {
          this.$store.commit('showMessage', {
            message: imError.message,
            type: 'error'
          })
        })
    },
    sendTextMessage() {
      if (
        this.messageContent === '' ||
        this.messageContent.trim().length === 0
      ) {
        this.messageContent = ''
        this.$store.commit('showMessage', {
          message: '不能发送空消息哦！',
          type: 'info'
        })
        return
      }
      const message = this.tim.createTextMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: { text: this.messageContent }
      })
      let content = this.messageContent
      this.$store.commit('pushCurrentMessageList', message)
      this.$bus.$emit('scroll-bottom')
      this.tim.sendMessage(message).then(() => {
        this.$parent.addToList([{
          type: this.TIM.TYPES.MSG_TEXT,
          from: this.userID,
          avatar: this.currentUserProfile.avatar,
          nick: this.currentUserProfile.nick,
          payload: { text: content }
        }])
      }).catch(error => {
        this.$store.commit('showMessage', {
          type: 'error',
          message: error.message
        })
      })
      this.messageContent = ''
    },
    sendCustomMessage() {
      if (
        this.form.data.length === 0 &&
        this.form.description.length === 0 &&
        this.form.extension.length === 0
      ) {
        this.$store.commit('showMessage', {
          message: '不能发送空消息',
          type: 'info'
        })
        return
      }
      const message = this.tim.createCustomMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: {
          data: this.form.data,
          description: this.form.description,
          extension: this.form.extension
        }
      })
      this.$store.commit('pushCurrentMessageList', message)
      this.tim.sendMessage(message).catch(error => {
        this.$store.commit('showMessage', {
          type: 'error',
          message: error.message
        })
      })
      Object.assign(this.form, {
        data: '',
        description: '',
        extension: ''
      })
      this.sendCustomDialogVisible = false
    },
    random(min, max) {
      return Math.floor(Math.random() * (max - min + 1) + min)
    },
    chooseEmoji(item) {
      this.messageContent += item
    },
    handleSendImageClick() {
      this.$refs.imagePicker.click()
    },
    handleSendFileClick() {
      this.$refs.filePicker.click()
    },
    handleSendVideoClick() {
      this.$refs.videoPicker.click()
    },
    videoCall() {
      this.$bus.$emit('video-call')
    },
    sendImage() {
      const message = this.tim.createImageMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: {
          file: document.getElementById('imagePicker') // 或者用event.target
        },
        onProgress: percent => {
          this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
        }
      })
      this.$store.commit('pushCurrentMessageList', message)
      this.tim
        .sendMessage(message)
        .then(() => {
        })
        .catch(imError => {
          this.$store.commit('showMessage', {
            message: imError.message,
            type: 'error'
          })
        })
      this.$refs.imagePicker.value = null
    },
    sendFile() {
      const message = this.tim.createFileMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: {
          file: document.getElementById('filePicker') // 或者用event.target
        },
        onProgress: percent => {
          this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
        }
      })
      this.$store.commit('pushCurrentMessageList', message)
      this.tim
        .sendMessage(message)
        .then(() => {
        })
        .catch(imError => {
          this.$store.commit('showMessage', {
            message: imError.message,
            type: 'error'
          })
        })
      this.$refs.filePicker.value = null
    },
    sendVideo() {
      const message = this.tim.createVideoMessage({
        to: this.toAccount,
        conversationType: this.currentConversationType,
        payload: {
          file: document.getElementById('videoPicker') // 或者用event.target
        },
        onProgress: percent => {
          this.$set(message, 'progress', percent) // 手动给message 实例加个响应式属性: progress
        }
      })
      this.$store.commit('pushCurrentMessageList', message)
      this.tim
        .sendMessage(message)
        .then(() => {
          this.$refs.videoPicker.value = null
        })
        .catch(imError => {
          this.$store.commit('showMessage', {
            message: imError.message,
            type: 'error'
          })
        })
    }
  }
}
</script>

<style lang="stylus" scoped>
#message-send-box-wrapper {
  box-sizing: border-box;
  overflow: hidden;
  padding: 10%;
}
.emoji {
  padding: 8px;
  font-size: 18px;
}

.emojis {
  height: 150px;
  box-sizing: border-box;
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  overflow-y: scroll;
}

.send-header-bar {
  box-sizing: border-box;
  padding: 3px 0 0 0;
  flex-direction: row,
}

.send-header-bar i {
  cursor: pointer;
  font-size: 24px;
  color: gray;
  margin: 0 12px 0 0;
}

.send-header-bar i:hover {
  color: $black;
}

textarea {
  resize: none;
}

.text-input {
  font-size: 16px;
  width: 72%;
  box-sizing: box-sizing;
  border: none;
  outline: none;
  background-color: transparent;
}

.block {
  padding: 10px 0;
  display: flex;
  align-items: center;
}

.bottom {
  position: fixed;
  bottom: 0%;
  width: 100%;
  height: 5%;
  left: 0%;
  z-index: 999;
  display: flex;
  flex-direction: row;
  align-items: center;
  background-color white


  .btn-send {
    cursor: pointer;
    position: absolute;
    color: $primary;
    font-size: 30px;
    right: 0;
    bottom: -5px;
    padding: 6px 6px 4px 4px;
    border-radius: 50%;
  }
}

.voice {
  padding: 4px;
}
</style>
