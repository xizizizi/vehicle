import { asyncRoutes, constantRoutes } from "@/router";
import store from "@/store";
import Layout from "@/layout";
import JSEncrypt from "jsencrypt";
/**
 * Use meta.role to determine if the current user has permission
 * @param roles
 * @param route
 */
function hasPermission(roles, route) {
  if (route.meta && route.meta.roles) {
    return roles.some((role) => route.meta.roles.includes(role));
  } else {
    return true;
  }
}

/**
 * Filter asynchronous routing tables by recursion
 * @param routes asyncRoutes
 * @param roles
 */
export function filterAsyncRoutes(routes, roles) {
  const res = [];

  routes.forEach((route) => {
    const tmp = { ...route };
    if (hasPermission(roles, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, roles);
      }
      res.push(tmp);
    }
  });

  return res;
}

const state = {
  routes: [],
  addRoutes: [],
};

const mutations = {
  SET_ROUTES: (state, routes) => {
    state.addRoutes = routes;
    state.routes = constantRoutes.concat(routes);
  },
};

const actions = {
  //获取用户角色生成路由
  generateRoutes({ commit }, roles) {
    return new Promise((resolve) => {
      let accessedRoutes;
      //将后端获取的菜单(String)转换为路由
      const asyncRoutes = [];
      const menus = store.getters.menus;
      console.log("获取的菜单:", menus);

      //   const menuList = [];
      //   if (menus && menus.length > 0) {
      //     for (let i = 0; i < menus.length; i++) {
      //       const obj = {};
      //       obj.name = menus[i].name;
      //       obj.path = menus[i].path;
      //       obj.redirect = menus[i].redirect;
      //       obj.meta = menus[i].meta;
      //       // 处理组件
      //       if (menus[i].component === "Layout") {
      //         obj.component = Layout;
      //       } else {
      //         const component = menus[i].component;
      //         obj.component = (resolve) =>
      //           require([`@/views/${component}`], resolve);
      //       }

      //       menuList.push(obj);
      //     }
      //   }

      //   asyncRoutes = menuList;
      if (roles.includes("admin")) {
        accessedRoutes = asyncRoutes || [];
      } else {
        accessedRoutes = filterAsyncRoutes(asyncRoutes, roles);
      }
      commit("SET_ROUTES", accessedRoutes);
      resolve(accessedRoutes);
    });
  },
};

export default {
  namespaced: true,
  state,
  mutations,
  actions,
};
