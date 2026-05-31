// 用户ID存储
const USER_ID_KEY = "ai_user_id";
const TODAY_MESSAGES_KEY = "ai_today_messages";
const TODAY_DATE_KEY = "ai_today_date";

// 获取用户ID
export function getUserId() {
  return localStorage.getItem(USER_ID_KEY);
}

// 保存用户ID
export function saveUserId(userId) {
  localStorage.setItem(USER_ID_KEY, userId);
}

// 获取今日消息数
export function getTodayMessages() {
  const today = new Date().toDateString();
  const savedDate = localStorage.getItem(TODAY_DATE_KEY);

  // 如果不是今天，重置计数
  if (savedDate !== today) {
    localStorage.setItem(TODAY_DATE_KEY, today);
    localStorage.setItem(TODAY_MESSAGES_KEY, "0");
    return 0;
  }

  return parseInt(localStorage.getItem(TODAY_MESSAGES_KEY) || "0");
}

// 保存今日消息数
export function saveTodayMessages(count) {
  localStorage.setItem(TODAY_MESSAGES_KEY, count.toString());
}

// 清除所有存储
export function clearAllStorage() {
  localStorage.removeItem(USER_ID_KEY);
  localStorage.removeItem(TODAY_MESSAGES_KEY);
  localStorage.removeItem(TODAY_DATE_KEY);
}
