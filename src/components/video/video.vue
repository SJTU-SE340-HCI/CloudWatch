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
            <input type="button" id="play" v-on:click="tongbu" value="同步播放"/>
        </div>
        <div>
            <input id="uploadVideoNow-file" type="file"  accept="video/*"/>
            <input type="button" value="上传本地视频" v-on:click="shangchuan"/>
        </div>
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
                RoomId:'x2RYna',
                url: ''
            }
        },
        mounted () {
            let self = this
            this.$nextTick(() => {
                setTimeout(() => {
                    let videoFileid = '5285890804129534917'
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
                return axios.post('http://localhost:8020/videoroom/signature')
                    .then(function (response) {
                        //alert(response.data)
                        return response.data
                    })
            },
            shangchuan() {
                let file = document.querySelector('input[type=file]')
                let mediaFile = file.files[0]
                const tcVod = new TcVod({
                    getSignature: this.getSignature // 前文中所述的获取上传签名的函数
                })
                const uploader = tcVod.upload({
                    mediaFile: mediaFile, // 媒体文件（视频或音频或图片），类型为 File
                })
                uploader.on('media_progress', function(info) {
                    window.console.log(info.percent) // 进度
                })
            }
        }
    }
</script>
