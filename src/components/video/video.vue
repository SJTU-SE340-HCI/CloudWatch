<template>
    <div id="detail" :class="isFull? 'full':'detail'">
        <div class="showvideolist" style="margin-top: 5px">
            <input type="button" value="浏览视频库" v-on:click="showVideoList"/>
        </div>
        <div class="videolist" style="margin-top: 5px" v-show="showList">
            <ul>
                <li v-for="(item,index) in list" :key="index" style="display: inline-block;margin-left: 10px">
                    <img :src="item.cover" style="width: 80px;height: 80px" @click="changeVideo(item.fileId)"/>
                    <p>{{item.name}}</p>
                    <p>视频简介：{{item.text}}</p>
                </li>
            </ul>
        </div>
        <div class="center bottoms" style="margin-top: 5px">
            <input id="uploadVideoNow-file" type="file" accept="video/*"/>
            <input type="button" value="上传本地视频" v-on:click="ShangChuan"/>
        </div>
        <div v-show="showJindu">
            <p>上传进度：</p>
            <div class="jindutiao">
                <span v-bind:style="{width: jindu+'%'}"></span>
            </div>
        </div>
        <div class="video" style="margin-top: 5px" v-show="showVideo" >
            <video width="640"
                   :id="tcPlayerId"
                   height="480"
                   class="tc-video-container"
                   playsinline
                   webkit-playinline
                   x5-playinline>
            </video>
            <input type="button" id="play" v-on:click="TongBu" value="同步播放"/>
        </div>
    </div>
</template>

<script>
    import axios from 'axios'
    import TcVod from 'vod-js-sdk-v6'

    export default {
        name: 'TcPlayer',
        props: {
            isFullscreen: Boolean,
        },
        computed: {
            isFull: function () {
                return this.isFullscreen
            }
        },
        data() {
            return {
                tcPlayerId: 'tcPlayer' + Date.now(),
                player: null,
                RoomId: 'x2RYna',
                showList:false,
                showVideo:false,
                showJindu:false,
                jindu:0,
                Fileid: '5285890804272485428',
                Appid: '1301931404',
                list: [
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'主题曲片花',
                        fileId:'5285890804129534917',
                        text:'简介1'
                    },
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'一路向北',
                        fileId:'5285890804272485428',
                        text:'简介2'
                    },
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'live',
                        fileId:'5285890801792789133',
                        text:'简介3'
                    },
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'浪姐',
                        fileId:'5285890804347869645',
                        text:'简介4'
                    },
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'庆佘年',
                        fileId:'5285890804447813743',
                        text:'简介5'
                    },
                    {
                        cover:require('../../assets/image/video.jpg'),
                        name:'浪姐预告',
                        fileId:'5285890804334475959',
                        text:'简介6'
                    }
                ]
            }
        },
        mounted() {
            let self = this
            this.$nextTick(() => {
                setTimeout(() => {
                    let videoFileid = this.Fileid
                    let videoAppid = this.Appid
                    self.getVideoLang(videoFileid, videoAppid)
                }, 400)
            })
        },
        methods: {
            // 初始化腾讯云播放器
            getVideoLang(fileID, appID) {
                const playerParam = {
                    fileID: fileID,
                    appID: appID
                }
                this.player = window.TCPlayer(this.tcPlayerId, playerParam)
            },
            playVideo() {
                let self = this
                let time = this.player.currentTime()
                //alert(time)
                axios.get('http://47.103.30.166:8020/videoroom/play', {
                    params: {
                        RoomId: this.RoomId,
                        curTime: time
                    }
                }).then(function (response) {
                    //alert(response.data)
                    //if(player.paused){
                    //  setInterval(pause,5000);
                    //}
                    if (Math.abs(response.data - time) > 0.5) {
                        self.player.currentTime(response.data + 1)
                    }
                })
            },
            TongBu() {
                //alert('test')
                this.player.on('playing', setInterval(this.playvideo, 2000))
            },

            getSignature() {
                return axios.post('http://47.103.30.166:8020/videoroom/signature')
                    .then(function (response) {
                        //alert(response.data)
                        return response.data
                    })
            },
            ShangChuan() {
                let self = this
                let file = document.querySelector('input[type=file]')
                let mediaFile = file.files[0]
                const tcVod = new TcVod({
                    getSignature: this.getSignature // 前文中所述的获取上传签名的函数
                })
                const uploader = tcVod.upload({
                    mediaFile: mediaFile, // 媒体文件（视频或音频或图片），类型为 File
                })
                uploader.on('media_progress', function (info) {
                    self.showJindu=true
                    window.console.log(info.percent) // 进度
                    self.jindu=info.percent*100
                })
                uploader.done().then(function (doneResult) {
                    self.showJindu=false
                    self.Fileid=doneResult.fileId
                    self.player.loadVideoByID({
                        fileID: self.Fileid, // 请传入需要播放的视频 filID（必须）
                        appID: self.Appid, // 请传入点播账号的 appID（必须）
                    })
                    self.showVideo=true
                })
            },

            showVideoList() {
                this.showList=!this.showList
            },
            changeVideo(fileID) {
                this.Fileid=fileID
                this.player.loadVideoByID({
                    fileID: this.Fileid, // 请传入需要播放的视频 filID（必须）
                    appID: this.Appid, // 请传入点播账号的 appID（必须）
                })
                this.showVideo=true
            }
        }
    }
</script>


<style>
.jindutiao{
    width: 250px;
    height: 20px;
    border: black 2px solid;
}
.jindutiao span{
    float: left;
    height: 20px;
    background: #66afe9;
}
</style>

