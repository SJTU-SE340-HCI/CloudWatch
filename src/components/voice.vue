<template>
  <div id="app">
    <el-button type="primary" @click="createClient">开麦</el-button>
    <el-button type="primary" @click="signOut">关麦</el-button>
    <div id='local_stream' class='video'></div>
    <template v-for="item in remoteVideo" >
      <div :id="'remote_stream'+ item" class='video' :key='item'></div>
    </template>
    {{remoteVideo}}
  </div>
</template>

<script>
import TRTC from 'trtc-js-sdk'
import {TEST_ACCOUNT} from '../const/account_dev'
export default {
  name: 'voiceCommunicate',
  components: {
  },
  data() {
    return {
      sdkAppId: TEST_ACCOUNT.sdkAppId,
      userId: TEST_ACCOUNT.userId + (Math.random() * 100).toFixed(),
      userToken: TEST_ACCOUNT.userToken,
      client: '',
      localStream: '',
      roomId: '1',
      remoteVideo: [],
      isSignUp: false
    }
  },
  created() {
  },
  methods: {
        voicecommunicate(){
            if(this.isOpenVoice===false)
            {
                this.isOpenVoice=true
                this.msg=this.msg2
                this.createClient()
            }else{
                this.isOpenVoice=false
                this.msg=this.msg1
                this.signOut()
            }
        },
       createClient() {
      //获取签名
      const config = this.genTestUserSig()
      const sdkAppId = config.sdkAppId
      const userSig = config.userSig
      this.client = TRTC.createClient({
        mode: 'videoCall',
        sdkAppId,
        userId: this.userId,
        userSig,
      });
      console.log(sdkAppId,this.userId,userSig,111111111)
      //注册远程监听，要放在加入房间前--这里用了发布订阅模式
      this.subscribeStream()
      this.joinRoom(this.roomId)

    },

    //加入房间
    joinRoom (roomId) {
      if(!this.isSignUp) {
        this.client.join({ roomId })
          .then(() => {
            console.log('进房成功');
            this.isSignUp = true;
            //创建本地流
            this.createStream(this.userId)
            // //播放远端流
            this.playRemoteStream(this.client)
          })
          .catch(error => {
            console.error('进房失败 ' + error);
          })
      } else {
        alert('不能重复加入直播间')
      }

    },

    createStream(userId) {
      const localStream = TRTC.createStream({ userId, audio: true, video: false });
      this.localStream =localStream 
     
      localStream
        .initialize()
        .catch(error => {
          console.error('初始化本地流失败 ' + error);
        })
        .then(() => {
          console.log('初始化本地流成功');
          localStream.play('local_stream');
          //创建好后才能发布
          this.publishStream(localStream)
        });
    },

    publishStream(localStream) {
      this.client
        .publish(localStream)
        .catch(error => {
          console.error('本地流发布失败 ' + error);
        })
        .then(() => {
          console.log('本地流发布成功');
        });
    },

    playRemoteStream() {
      this.client.on('stream-subscribed', event => {
        const remoteStream = event.stream;
        console.log('远端流订阅成功：' + remoteStream.getId());
        this.remoteVideo.push(remoteStream.getId())
        // 播放远端流
        this.$nextTick(()=>{
        remoteStream.play('remote_stream'+ remoteStream.getId());
        },1000)
        
      })

    },

    //订阅远端流--加入房间之前
    subscribeStream () {
      this.client.on('stream-added', event => {
        const remoteStream = event.stream;
        console.log('远端流增加: ' + remoteStream.getId());
        //订阅远端流
        this.client.subscribe(remoteStream);
      });
    },

    signOut() {
      this.isSignUp=false;
      this.remoteVideo=[];
      this.client
      .leave()
      .then(() => {
        // 退房成功，可再次调用client.join重新进房开启新的通话。
        console.log('退出直播间')
      })
      .catch(error => {
        console.error('退出直播间失败 ' + error);
        // 错误不可恢复，需要刷新页面。
      });
    },

    genTestUserSig() {
      const USERID = this.userId
      const SDKAPPID = this.sdkAppId;
      const EXPIRETIME = 604800;
      const SECRETKEY = this.userToken;
      if (SDKAPPID === '' || SECRETKEY === '') {
        alert(
          '请先配置好您的账号信息： SDKAPPID 及 SECRETKEY ' +
            '\r\n\r\nPlease configure your SDKAPPID/SECRETKEY in js/debug/GenerateTestUserSig.js'
        );
      }
      const generator = new window.LibGenerateTestUserSig(SDKAPPID, SECRETKEY, EXPIRETIME);
      const userSig = generator.genTestUserSig(USERID);
      return {
        sdkAppId: SDKAPPID,
        userSig: userSig
      };
    }
    }
}
</script>

