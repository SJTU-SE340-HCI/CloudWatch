<template>
    <div class="container">
        <div id="detail" :class="isFull? 'full':'detail'" v-show="showVideo">
            <video
                    :id="tcPlayerId"
                    class="tc-video-container"
                    playsinline
                    webkit-playinline
                    x5-playinline>
            </video>
        </div>
        <div v-if="!showVideo" class="center">
            <div class="showvideolist">
                <el-button type="primary" round @click="showVideoList">浏览视频库</el-button>
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
            <el-upload
                    action=""
                    :limit="1"
                    drag
                    :auto-upload="false"
                    list-type="picture"
                    :on-change="ShangChuan"
                    class="center"
                    :on-remove="cancelUploader"
            >
                <div class="el-upload__tip">只能上传常见视频格式文件</div>
                <i class="el-icon-upload"></i>
                <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            </el-upload>
            <!--        <div class="" style="margin-top: 5px">-->
            <!--          <input id="uploadVideoNow-file" type="file" accept="video/*"/>-->
            <!--          <input type="button" value="上传本地视频" v-on:click="ShangChuan"/>-->
            <!--        </div>-->
            <div class="progress" v-show="showJindu">
                <el-progress :text-inside="true" :stroke-width="30" :percentage="parseInt(jindu)"></el-progress>
            </div>
        </div>
    </div>
</template>

<script>
    import axios from 'axios'
    import TcVod from 'vod-js-sdk-v6'
    import {Progress, Upload} from 'element-ui'

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
        components: {
            ElUpload: Upload,
            ElProgress: Progress
        },
        data() {
            return {
                tcPlayerId: 'tcPlayer' + Date.now(),
                player: null,
                showList: false,
                showVideo: false,
                showJindu: false,
                isPause:false,
                uploader: null,
                jindu: 0,
                Fileid: null,
                Appid: '1301931404',
                list: [
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: '主题曲片花',
                        fileId: '5285890804129534917',
                        text: '简介1'
                    },
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: '一路向北',
                        fileId: '5285890804272485428',
                        text: '简介2'
                    },
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: 'live',
                        fileId: '5285890801792789133',
                        text: '简介3'
                    },
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: '浪姐',
                        fileId: '5285890804347869645',
                        text: '简介4'
                    },
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: '庆佘年',
                        fileId: '5285890804447813743',
                        text: '简介5'
                    },
                    {
                        cover: require('../../assets/image/video.jpg'),
                        name: '浪姐预告',
                        fileId: '5285890804334475959',
                        text: '简介6'
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
                  axios.get('http://47.103.30.166:8020/Room/findById', {
                      params: {
                          room_id: this.$store.state.currentRoomId
                      }
                  }).then(res => {
                      if (res.data.videoFileId != undefined || res.data.videoFileId != null) {
                          this.$store.commit('setVideoRoomId', this.Fileid)
                          this.player.loadVideoByID({
                              fileID: res.data.videoFileId, // 请传入需要播放的视频 filID（必须）
                              appID: this.Appid, // 请传入点播账号的 appID（必须）
                          })
                          this.showVideo=true
                          this.$parent.showVideo=true
                      }
                  })
              }, 400)
            })
        },
        methods: {
            // 初始化腾讯云播放器
            getVideoLang(fileID, appID) {
                const playerParam = {
                    fileID: this.Fileid,
                    appID: appID,
                    controlBar: {
                        fullscreenToggle: false,
                        playbackRateMenuButton: false
                    }
                }
                this.player = window.TCPlayer(this.tcPlayerId, playerParam)
                this.player.on('playing', () => setInterval(this.playVideo, 2000))

                if (this.$store.state.currentRoom.kindRoom == 1) {
                    this.player.on('play', this.beginRecord)
                    this.player.on('pause', this.finishRecord)
                }
            },

            // 视频状态改变回调函数，用户录制
            beginRecord() {
                this.$store.commit('setVideoRoomId', this.Fileid)
                axios.get('http://47.103.30.166:8020/Room/setSDK', {
                    params: {
                        room_id: this.$store.state.currentRoomId,
                        video_id: this.$store.state.currentVideoRoomId
                    }
                })
                this.$parent.beginRecord()
            },
            finishRecord() {
                this.$parent.finishRecord()
            },

            playVideo() {
                let self = this
                if (this.player != null) {
                    let time = this.player.currentTime()
                    //alert(time)
                    axios.get('http://47.103.30.166:8020/videoroom/play', {
                        params: {
                            roomid: this.$store.state.currentRoomId,
                            //RoomId: '666',
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
                }
            },

            getSignature() {
                return axios.post('http://47.103.30.166:8020/videoroom/signature')
                    .then(function (response) {
                        //alert(response.data)
                        console.log(response)
                        return response.data
                    })
            },
            cancelUploader() {
                this.uploader.cancel()
                this.showJindu = false
            },
            ShangChuan(event) {
                let self = this
                if (this.uploader != null) {
                    this.uploader.cancel()
                }
                //let file = document.querySelector('input[type=file]')
                //let mediaFile = file.files[0]
                let mediaFile = event.raw
                const tcVod = new TcVod({
                    getSignature: this.getSignature // 前文中所述的获取上传签名的函数
                })
                this.uploader = tcVod.upload({
                    mediaFile: mediaFile, // 媒体文件（视频或音频或图片），类型为 File
                })
                this.uploader.on('media_progress', function (info) {
                    self.showJindu = true
                    window.console.log(info.percent) // 进度
                    self.jindu = info.percent * 100
                })
                this.uploader.done().then( doneResult => {
                    self.showJindu = false
                    self.Fileid = doneResult.fileId
                    axios.get('http://47.103.30.166:8020/Room/setVideo', {
                        params: {
                            room_id: self.$store.state.currentRoomId,
                            videoFileId: self.Fileid
                        }
                    }).then(res => {
                        window.console.log(res)
                        this.$store.commit('setVideoRoomId', self.Fileid)
                        self.player.loadVideoByID({
                            fileID: self.Fileid, // 请传入需要播放的视频 filID（必须）
                            appID: self.Appid, // 请传入点播账号的 appID（必须）
                        })
                        self.showVideo = true
                        self.$parent.showVideo = true
                    })
                })
            },
            showVideoList() {
                this.showList = !this.showList
            }
            ,
            changeVideo(fileID) {
                if (this.uploader != null) {
                    this.uploader.cancel()
                }

                this.Fileid = fileID
                axios.get('http://47.103.30.166:8020/Room/setVideo', {
                    params: {
                        room_id: this.$store.state.currentRoomId,
                        videoFileId: this.Fileid
                    }
                }).then(res => {
                    this.$store.commit('setVideoRoomId', this.Fileid)
                    this.player.loadVideoByID({
                        fileID: this.Fileid, // 请传入需要播放的视频 filID（必须）
                        appID: this.Appid, // 请传入点播账号的 appID（必须）
                    })
                    this.showVideo = true
                    this.$parent.showVideo = true
                })
            }
        }
    }


</script>

<style lang='stylus' scoped>
    .container {
        width: 100%;
        height: 100%;
    }

    .detail {
        padding-left: 15%;
        padding-right: 15%;
        padding-top: 5%;
        height: 70%;
    }

    .full {
        padding: 0;
        width: 100.5%;
        left: -0.25%;
        height: 95.25%;
        top: -0.25%;
    }

    .video {
        width: 100%;
        height: 100%;
        margin: 0;
    }

    .buttons {
        height: 3%;
    }

    .tc-video-container {
        width: 100%;
        height: 100%;
    }

    .center {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .jindutiao {
        width: 250px;
        height: 20px;
        border: black 2px solid;
    }

    .jindutiao span {
        float: left;
        height: 20px;
        background: #66afe9;
    }

    .showvideolist {
        padding: 5% 10%;
    }

    .progress {
        padding: 10% 12% 0%;
        width: 100%;
        height: 100%;
    }

    @import url("//unpkg.com/element-ui@2.13.2/lib/theme-chalk/index.css");
</style>
