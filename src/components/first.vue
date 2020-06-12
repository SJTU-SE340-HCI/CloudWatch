<template>
  <div id="detail" :class="isFull? 'full':'detail'">
    <div class="video">
      <video width=""
             :id="tcPlayerId"
             height="100%"
             class="tc-video-container"
             playsinline
             webkit-playinline
             x5-playinline>
      </video>
    </div>
    <div class="center bottoms">
      <input type="button" id="play" v-on:click="tongbu" value="同步播放"/>
      <input type="button" id="shangchuan" v-on:click="shangchuan" value="上传本地视频"/>
    </div>
  </div>
</template>

<script>
  import axios from 'axios'
  import TcVod from 'vod-js-sdk-v6'

  export default {
    name: 'TcPlayer',

    props: {
      isFullscreen:Boolean,
    },
    computed: {
      isFull: function () {
        return this.isFullscreen
      }
    },
    data () {
      return {
        tcPlayerId: 'tcPlayer' + Date.now(),
        player: null,
        RoomId:'x2RYna'
      }
    },
    mounted () {
      let self = this
      this.$nextTick(() => {
        setTimeout(() => {
          let videoFileid = '5285890801792789133'
          let videoAppid = '1301931404'
          self.getVideoLang(videoFileid, videoAppid)
        }, 400)
      })
    },
    methods: {
      // 初始化腾讯云播放器
      getVideoLang (fileID, appID) {
        const playerParam = {
          fileID: fileID,
          appID: appID
        }
        this.player = window.TCPlayer(this.tcPlayerId, playerParam)
      },
      playvideo() {
        let self = this
        let time = this.player.currentTime()
        //alert(time)
        axios.get('http://localhost:8020/videoroom/play',{
          params:{
            RoomId: this.RoomId,
            curTime: time
          }
        }).then(function (response) {
          //alert(response.data)
          //if(player.paused){
          //  setInterval(pause,5000);
          //}
          //checkplayer();
          if (Math.abs(response.data-time)>0.5) {
            self.player.currentTime(response.data+1)
          }
        })
      },
      tongbu() {
        //alert('test')
        this.player.on('playing',setInterval(this.playvideo,2000))
      },
      getSignature() {
        return axios.post(url).then(function (response) {
          return response.data.signature
        })
      },
      shangchuan() {

      }
    }
  }
</script>

<style lang='stylus' scoped>
  .detail{
    padding-left: 15%;
    padding-right: 15%;
    height: 85%;
  }
  .full{
    padding: 0;
    width: 100.5%;
    left: -0.25%;
    height: 95.25%;
    top: -0.25%;

  }
  .video{
    width: 100%;
    height: 97%;
    margin: 0;
  }
  .buttons{
    height: 3%;
  }
  .tc-video-container{
    width: 100%;
    height: 100%;
  }
  .center{
    justify-content: center;
    align-items: center;
  }
</style>