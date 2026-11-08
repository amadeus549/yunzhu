const TOKEN_KEY = 'hestia_token'
const INFO_KEY = 'hestia_user'

const getInfo = () => {
  try {
    return JSON.parse(localStorage.getItem(INFO_KEY)) || {}
  } catch (e) {
    return {}
  }
}

const state = {
  token: localStorage.getItem(TOKEN_KEY) || '',
  name: getInfo().name || '',
  username: getInfo().username || '',
  role: getInfo().role || null,
  roleName: getInfo().roleName || ''
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem(TOKEN_KEY, token)
  },
  SET_INFO(state, info) {
    Object.assign(state, info)
    localStorage.setItem(INFO_KEY, JSON.stringify(info))
  },
  CLEAR(state) {
    state.token = ''
    state.name = ''
    state.username = ''
    state.role = null
    state.roleName = ''
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(INFO_KEY)
  }
}

const actions = {
  login({ commit }, data) {
    commit('SET_TOKEN', data.token)
    commit('SET_INFO', {
      name: data.name,
      username: data.username,
      role: data.role,
      roleName: data.roleName
    })
  },
  logout({ commit }) {
    commit('CLEAR')
  }
}

const getters = {
  token: state => state.token,
  name: state => state.name,
  roleName: state => state.roleName
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
