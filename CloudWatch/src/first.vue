<template>
  <div>
    <index/>
    <h2 style="align-content: center">视频播放demo</h2>
    <iframe src="/static/video.html" scrolling="no" style="width: 800px;height: 600px;" frameborder=”no”></iframe>

  </div>
</template>

<script>
    import Index from './index.vue'


    export default {
        name:'player',
        components:{
            Index,
        }
    }



</script>

