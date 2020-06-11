<template>
<<<<<<< HEAD
  <div id="detail">
    <div>
      <video width="640"
             :id="tcPlayerId"
             height="480"
             class="tc-video-container"
             playsinline
             webkit-playinline
             x5-playinline>
      </video>
    </div>
    <div>
      <input type="button" id="play" v-on:click="tongbu" value="同步播放"/>
      <input type="button" id="shangchuan" v-on:click="shangchuan" value="上传本地视频"/>
    </div>
=======
  <div>
    <iframe src="./static/video.html" scrolling="no" style="width: 100%; height: 100%; padding-top: 10px;" frameborder=”no”></iframe>
>>>>>>> 4787be51e7d0b77ab67b66d09ca6aef9185af10b
  </div>
</template>

<script>
    import axios from 'axios'
    import TcVod from 'vod-js-sdk-v6'

    export default {
        name: 'TcPlayer',
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
                    return response.data.signature;
                })
            },
            shangchuan() {

            }




        }
    }
</script>
