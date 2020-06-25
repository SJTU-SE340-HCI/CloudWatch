<template>
  <div class="container">
    <el-button type="primary"  @click="beginRecord" v-if="!isRecording" >录制</el-button>
    <el-button type="primary"  @click="finishRecord" v-else>结束录制</el-button>
    <el-button type="primary" @click="presentRecord">回放</el-button>
  </div>
</template>

<script>
import axios from 'axios'
export default {
    data() {
        return {
            isRecording:false,
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
            this.$store.commit('setVideoRoomId', '12')
            this.$store.commit('clearBarrage')
        },
        finishRecord() {
            this.isRecording=false
            /*
            结束连麦
            将vuex中的弹幕消息发送到后端
            */
            this.$store.commit('setRecordState',false)

            // 将vuex中的弹幕消息发送到后端
            axios({
            method: 'post',
            url: 'http://47.103.30.166:8020/Room/textRecord/Receive',
            headers: {}, 
            params: {
                video_id: 12
            },
            data: this.$store.getters.getBarrages
            });


        },
        presentRecord() {
            /*
            从后端拿到弹幕消息
            根据当前开始的时间轮询弹幕消息的队列
            */
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