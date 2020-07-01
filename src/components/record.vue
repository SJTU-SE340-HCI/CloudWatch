<template>
  <div class="container">
    <el-button type="primary"  @click="beginRecord" v-if="!isRecording" >录制</el-button>
    <el-button type="primary"  @click="finishRecord" v-else>结束录制</el-button>
    <el-button type="primary" @click="presentRecord" v-if="!isPresenting">回放</el-button>
    <el-button type="primary"  @click="finishPresent" v-else>结束回放</el-button>
  </div>
</template>

<script>
import axios from 'axios'
import {Howl, Howler} from 'howler';
export default {
    data() {
        return {
            isPresenting: false,
            isRecording:false,
            recordedBarrages: new Map(),
            recordedVoices: []
        }
    },
    methods: {
        beginRecord() {
            this.isRecording=true
            /*
            将开始时间存入vuex
            将录制中状态存入vuex，让弹幕发送时将弹幕信息存入vuex
            开始就自动打开连麦按钮，用户可以选择关闭
            */
            this.$store.commit('setRecordState',true)
            this.$store.commit('setBeginRecordTime')
            this.$store.commit('clearBarrage')
            /*if(this.recordVoice) {
                this.recordVoice(true)
            }*/
        },
        finishRecord() {
            this.isRecording=false
            
            /* 停止录制结束连麦 */
            this.$store.commit('setRecordState',false)
            /*if(this.recordVoice) {
                this.recordVoice(false)
            }*/

            // 将vuex中的弹幕消息发送到后端
            axios({
            method: 'post',
            url: 'http://47.103.30.166:8020/Room/textRecord/Receive',
            headers: {}, 
            params: {
                video_id: this.$store.getters.getCurrentVideoId
            },
            data: this.$store.getters.getBarrages
            })
        },

        presentRecord() {
            this.isPresenting=true
            // 从后端拿到弹幕消息
            var videoid = new Number(this.$store.getters.getCurrentVideoId)
            axios
            .get('http://47.103.30.166:8020/Room/textRecord/findByVideoId', {
            params: {
               video_id: videoid
            }
            })
            .then(res => {
              console.log(res)
              this.recordedBarrages = res.data
            })
            
            // 从后端拿到聊天音频
            axios.get('http://47.103.30.166:8020/Room/voiceRecord/findPreviousRecord', {
                params:{
                    room_id: this.$store.state.currentRoomId
                }
            })
            .then(res => {
                this.recordedVoices=res.data
                console.log(this.recordedVoices[0])
            })

            var sound =  new Howl({
                src: 'http://1301703207.vod2.myqcloud.com/e0ee959avodcq1301703207/78b6c3ee5285890804744856150/f0.aac',
                html5: true, // A live stream can only be played through HTML5 Audio.
                format: ['mp3', 'aac']
            })
            
            // Begin playing the sound.
            sound.play()

            // 根据当前开始的时间轮询弹幕消息的队列
            const timeId = setInterval(() => {
                if (this.isPresenting == false) {
                clearInterval(timeId)
                }
                this.sendReadyBarrage()
            },1000)
        },

        finishPresent() {
            this.isPresenting=false
        },

        sendReadyBarrage() {
            Object.entries(this.recordedBarrages).map((key, value) => {
                    var barrageTime = Number(key[0])+ Number(this.$store.getters.getRecordingBeginTime)
                    var cur=Date.now()
                    if( barrageTime>= cur && barrageTime<cur+1000)
                    {
                        console.log('hit')
                        console.log(barrageTime, cur)
                        console.log(key[1])
                        this.$parent.sendBarrage(key[1])
                    }
            })
        },

        beginVoiceCommunicate() {
            this.$refs.VoiceCommunicate.createClient()
        }


    }
}
</script>

<style lang='stylus' scoped>
.container{
    width: 100%;
    height: 100%;
  }
</style>