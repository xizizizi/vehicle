/**
 * Created by PanJiaChen on 16/11/18.
 */

/**
 * @param {string} path
 * @returns {Boolean}
 */
export function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * @param {string} str
 * @returns {Boolean}
 */
/**
 * 验证用户名
 * @param {string} str
 * @returns {Boolean}
 */
export function validUsername(str) {
    // 这里你可以根据实际需求修改验证规则
    // 例如：只允许字母数字组合，长度1-20位
    const validMap = ['admin', 'editor', 'user'] // 示例：允许的用户名列表
    return validMap.indexOf(str.trim()) >= 0 || str.trim().length >= 1
  }
  
  /**
   * 验证邮箱
   * @param {string} email
   * @returns {Boolean}
   */
  export function validEmail(email) {
    const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return reg.test(email)
  }
  
  /**
   * 验证手机号
   * @param {string} phone
   * @returns {Boolean}
   */
  export function validPhone(phone) {
    const reg = /^1[3-9]\d{9}$/
    return reg.test(phone)
  }
  
  /**
   * 验证URL
   * @param {string} url
   * @returns {Boolean}
   */
  export function validURL(url) {
    const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
    return reg.test(url)
  }
  
  /**
   * 验证小写字母
   * @param {string} str
   * @returns {Boolean}
   */
  export function validLowerCase(str) {
    const reg = /^[a-z]+$/
    return reg.test(str)
  }
  
  /**
   * 验证大写字母
   * @param {string} str
   * @returns {Boolean}
   */
  export function validUpperCase(str) {
    const reg = /^[A-Z]+$/
    return reg.test(str)
  }
  
  /**
   * 验证字母
   * @param {string} str
   * @returns {Boolean}
   */
  export function validAlphabets(str) {
    const reg = /^[A-Za-z]+$/
    return reg.test(str)
  }
  
  /**
   * 验证身份证号
   * @param {string} idCard
   * @returns {Boolean}
   */
  export function validIdCard(idCard) {
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
    return reg.test(idCard)
  }
