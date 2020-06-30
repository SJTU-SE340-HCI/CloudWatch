import Vue from 'vue'
import Vuex from 'vuex'
import conversation from './modules/conversation'
import group from './modules/group'
import user from './modules/user'
import video from './modules/video'
import friend from './modules/friend'
import blacklist from './modules/blacklist'
import {Message} from 'element-ui'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    current: Date.now(), // 当前时间
    intervalID: 0,
    message: undefined,
    currentRoom:{},
    currentRoomId: '',
    currentVideoRoomId:Number,
    isRecording: false,
    isPresenting: false,
    beginRecordingTime: Number,
    barrages: new Map(),
    priorBarrages: new Map()
  },
  getters: {
    hidden(state) {
      // eslint-disable-next-line no-unused-vars
      const temp = state.current 
      if (typeof document.hasFocus !== 'function') {
        return document.hidden
      }
      return !document.hasFocus()
    },
    getRecordingState(state) {
      return state.isRecording
    },
    getRecordingBeginTime(state) {
      return state.beginRecordingTime
    },
    getCurrentVideoId(state) {
      return state.currentVideoRoomId
    },
    getBarrages(state) {
      return state.barrages
    },
    getPriorBarrages(state) {
      return state.priorBarrages
    },
    getPresentingState(state) {
      return state.isPresenting
    }

  },
  mutations: {
    startComputeCurrent(state) {
      state.intervalID = setInterval(() => {
        state.current = Date.now()
      }, 500)
    },
    stopComputeCurrent(state) {
      clearInterval(state.intervalID)
      state.intervalID = 0
    },
    showMessage(state, options) {
      if (state.message) {
        state.message.close()
      }
      state.message = Message({
        message: options.message,
        type: options.type || 'success',
        duration: options.duration || 2000,
        offset: 40
      })
    },
    changeRoom(state,room)
    {
      state.currentRoom=room
    },

    changeRoomId(state,roomId)
    {
      state.currentRoomId=roomId
    },

    setRecordState(state, isrecording)
    {
      state.isRecording=isrecording
    },

    setPresentState(state, ispresenting)
    {
      state.isPresenting=ispresenting
    },

    setBeginRecordTime(state)
    {
      state.beginRecordingTime=Date.now()
    },

    setVideoRoomId(state, videoFileId)
    {
      var fileid=new Number(parseInt(videoFileId,10))
      state.currentVideoRoomId=fileid&0xffffff
      console.log(state.currentVideoRoomId)
    },

    addBarrage(state,barrageContent)
    {
      var time= Date.now()-state.beginRecordingTime
      state.barrages[time]=barrageContent
      console.log(state.barrages)
    },

    clearBarrage(state) {
      state.barrages=new Map()
    },

    setPriorBarrage(state, barrageList)
    {
      state.priorBarrages=barrageList
    }

  },
  modules: {
    conversation,
    group,
    friend,
    blacklist,
    user,
    video
  }
})
