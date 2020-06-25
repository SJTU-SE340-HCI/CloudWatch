<template>
  <div>
    <el-button type="primary"  v-if="!isRecording" >录制</el-button>
    <el-button type="primary"  v-else>结束录制</el-button>
    <el-button type="primary">回放</el-button>
  </div>
</template>

<script>
export default {
    data() {
        return {
            isRecording:false,
        }
    }
}
</script>

<style scoped>

</style>