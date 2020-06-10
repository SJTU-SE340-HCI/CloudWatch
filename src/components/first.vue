<template>
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
      <input type="button" id="play" v-on:click="playvideo" value="同步播放"/>
    </div>
  </div>
</template>

<script>
    import axios from 'axios'

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
                alert(time)
                //var id = document.getElementById("roomid").value;
                axios.get('http://localhost:8020/videoroom/play',{
                    params:{
                        RoomId: this.RoomId,
                        curTime: time
                    }
                }).then(function (response) {
                    alert(response.data)
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





        }
    }
</script>
