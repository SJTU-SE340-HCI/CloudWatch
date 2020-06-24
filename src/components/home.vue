<template>
  <div>
    <homepage v-if="!isRoom" @enterRoom="enterRoom"/>
    <room ref="room" v-else />
  </div>
</template>

<script>
  import Room from './room'
  import Homepage from './homepage'

  export default {
    name: 'home',

    components: {
      Room,
      Homepage
    },

    data () {
      return {
        isRoom: false,
      }
    },

    methods: {
      addToList(messageList) {
        this.$refs.room.addToList(messageList)
      },

      enterRoom() {
        this.isRoom = true
      }
    }
  }
</script>

<style lang="stylus">

</style>