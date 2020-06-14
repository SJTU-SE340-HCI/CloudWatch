<template>
  <div>
    <homepage v-if="!isRoom" @enterRoom="enterRoom"/>
    <template v-else>
      <div class="baberrage">
        <vue-baberrage
                :isShow= "barrageIsShow"
                :barrageList = "barrageList"
                :loop = "barrageLoop"
        >
        </vue-baberrage>
      </div>
      <room/>
    </template>
  </div>
</template>

<script>
  import { MESSAGE_TYPE } from 'vue-baberrage'
  import Room from './room'
  import Homepage from './homepage'

  export default {
    name: 'home',

    components: {
      Room,
      Homepage
    },

    data () {
      return {
        msg: 'Hello vue-baberrage',
        barrageIsShow: true,
        currentId : 0,
        barrageLoop: false,
        barrageList: [],
        showChatRoom: false,
        isFullscreen: false,
        isRoom: false,
      }
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
      enterRoom() {
        this.isRoom = true
      }
    }
  }
</script>

<style lang="stylus">
  .baberrage {
    width: 100%;
    height: 30%;
    margin: 0;
    position: relative;
    z-index: 3;
  }
</style>