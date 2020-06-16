<template>
  <div>
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
            v-show="!showChatRoom">
        <i class="iconfont icon-zuojiantou"></i>
      </div>
    </transition>
    <player class="player" :isFullscreen="this.isFullscreen"/>
    <el-button class="fullscreen" @click="screenfull">
      全屏
    </el-button>
    <call-layer ref="callLayer" class="chat-wrapper"/>
    <image-previewer />
    <div class="bottom" v-if="isFullscreen">
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
      }
    },

    mounted() {
      window.onresize = () => {
        if (this.checkFull()) {
          this.isFullscreen = !this.isFullscreen
        }
      }
    },

    methods: {
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
      /**
       * 是否全屏并按键ESC键的方法
       */
      checkFull() {
        var isFull = document.fullscreenEnabled || window.fullScreen || document.webkitIsFullScreen || document.msFullscreenEnabled

        if (isFull === undefined) {
          isFull = false
        }
        return isFull
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
