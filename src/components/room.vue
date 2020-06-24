<template>
  <div
          class="container"
          @mousemove="checkmouse($event)"
  >
    <div class="baberrage" v-if="isFullscreen">
      <vue-baberrage
              :isShow= "barrageIsShow"
              :barrageList = "barrageList"
              :loop = "barrageLoop"
      >
      </vue-baberrage>
    </div>
    <transition name="chatroom">
      <div class="chat-wrapper" v-if="showChatRoom">
        <el-row>
          <!--          <el-col :xs="10" :sm="10" :md="8" :lg="8" :xl="7">-->
          <!--            <side-bar />-->
          <!--          </el-col>-->
          <el-col :xs="14" :sm="14" :md="24" :lg="16" :xl="17">
            <current-conversation />
          </el-col>
        </el-row>
      </div>
    </transition>
    <transition name="chatroom">
      <div class="float wrap" style="cursor: pointer"  @click="showChatRoom = !showChatRoom"
           v-show="showChatRoom">
        <i class="iconfont icon-youjiantou"></i>
      </div>
    </transition>
    <transition name="float">
      <div  class="chatroom-float wrap" style="cursor: pointer" @click="showChatRoom = !showChatRoom"
            v-show="!showChatRoom && showChatRoomButton">
        <i class="iconfont icon-zuojiantou"></i>
      </div>
    </transition>
    <player class="player" :isFullscreen="this.isFullscreen"/>
    <el-button class="fullscreen" @click="screenfull" v-show="showFullButton">
      {{this.isFullscreen? '退出全屏' : '全屏'}}
    </el-button>
    <call-layer ref="callLayer" class="chat-wrapper"/>
    <image-previewer />
    <div class="bottom" v-show="isFullscreen && showBottom">
      <message-send-box />
    </div>
  </div>
</template>

<script>
  import MessageSendBox from './message/message-bottom-send-box'
  import CurrentConversation from './conversation/current-conversation'
  import ImagePreviewer from './message/image-previewer'
  import CallLayer from './message/call-layer'
  import Player from './video/video'
  import screenfull from 'screenfull'
  import { MESSAGE_TYPE } from 'vue-baberrage'

  export default {
    name: 'Room',
    components: {
      MessageSendBox,
      //SideBar,
      CurrentConversation,
      ImagePreviewer,
      CallLayer,
      Player,
    },

    data () {
      return {
        showChatRoom: false,
        isFullscreen: false,
        showFullButton: true,
        showChatRoomButton: true,
        showBottom: false,
        msg: 'Hello vue-baberrage',
        barrageIsShow: true,
        currentId : 0,
        barrageLoop: false,
        barrageList: [],
      }
    },

    mounted() {
      this.$nextTick(()=>{
        document.addEventListener('fullscreenchange',(e)=>{
          this.toggleFullScreen()
        })

      })
    },

    methods: {
      addToList(messageList) {
        const groupMessageList = messageList.filter(
          message => message.type === this.TIM.TYPES.MSG_TEXT
        )
        let avatar = groupMessageList[0].avatar == '' ? 'https://imgcache.qq.com/open/qcloud/video/act/webim-avatar/avatar-2.png' : groupMessageList[0].avatar
        let nick = groupMessageList[0].nick == '' ? groupMessageList[0].from : groupMessageList[0].nick
        this.barrageList.push({
          id: ++this.currentId,
          avatar: avatar,
          msg: nick + ':' + groupMessageList[0].payload.text,
          time: 10,
          type: MESSAGE_TYPE.NORMAL,
          extraWidth: 2,
        })
      },

      screenfull() {
        if (!screenfull.isEnabled) {
          this.$message({
            message: 'Your browser does not work',
            type: 'warning'
          })
          return false
        }
        screenfull.toggle()
      },
      toggleFullScreen() {
        this.isFullscreen = !this.isFullscreen
        this.showFullButton = this.isFullscreen? false : true
        this.showChatRoomButton = this.isFullscreen? false : true
      },
      checkmouse(event) {
        if (event.clientX < 10) {
          this.showFullButton = true
        }

        if (event.clientX > 80 && this.isFullscreen) {
          this.showFullButton = false
        }

        if (event.clientX > document.body.clientWidth - 10) {
          this.showChatRoomButton = true
        }

        if (event.clientX < document.body.clientWidth - 100 && this.isFullscreen) {
          this.showChatRoomButton = false
        }

        if (event.clientY > document.body.clientHeight - 70) {
          this.showBottom = true
        }

        if (event.clientY < document.body.clientHeight - 100) {
          this.showBottom = false
        }
      }
    }
  }
</script>

<style lang="stylus">
  .container {
    width: 100%;
    height: 100%;
  }

  .chat-wrapper {
    margin-top: 8vh;
    width: $width;
    height: $height;
    max-width: 1280px;
    position: fixed;
    right: 0%;
    z-index: 999;

    .official-link {
      display: flex;
      text-decoration: none;
      color: #38c9ff;
      width: fit-content;
      float: right;
      height: 45px;
      align-items: center;
    }
  }

  .chatroom{
    height: 100%;
    width: 700px;
    position: fixed;
    right: 0%;
    top: 0%;
    float: right;
    z-index: 999;
    overflow: auto;
  }

  .chatroom-enter-active {
    transition: all 0.5s ease;
  }
  .chatroom-leave-active {
    transition: all .5s ease;
  }
  .chatroom-enter, .chatroom-leave-to{
    transform: translateX(50px);
    opacity: 0;
  }

  .chatroom-float{
    width: 50px;
    height: 50px;
    border-radius: 50px;
    float: right;
    position: fixed;
    top: 50%;
    right: 2%;
    z-index: 999;
  }

  .float{
    width: 50px;
    height: 50px;
    border-radius: 50px;
    float: right;
    position: fixed;
    top: 50%;
    right: 10%;
    z-index: 999;
  }

  .bottom{
    position: fixed;
    bottom: 0%;
    width: 100%;
    height: 5%;
    left: 0%;
    z-index: 999;
  }

  .player{
    position: fixed;
    left: 0%;
    height: 0%;
    width: 100%;
    height: 90%;
    z-index: 2;
  }

  .float-enter-active {
    transition: all .8s ease;
  }
  .float-enter, .float-leave-to{
    transform: translateX(-60px);
    opacity: 0;
  }

  .wrap {
    background-color: #ffffff;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .fullscreen {
    position: fixed;
    left: 1%;
    top: 50%;
    float: left;
    z-index: 999;
  }

  .baberrage {
    width: 100%;
    height: 30%;
    margin: 0;
    position: relative;
    z-index: 3;
  }

  /* 设置滚动条的样式 */
  ::-webkit-scrollbar {
    width: 3px;
    height: 3px;
  }

  /* 滚动槽 */
  ::-webkit-scrollbar-track {
    border-radius: 10px;
  }

  /* 滚动条滑块 */
  ::-webkit-scrollbar-thumb {
    border-radius: 10px;
    background: rgba(0, 0, 0, 0.1);
  }
</style>
