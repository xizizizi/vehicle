// import Layout from "@/layout/index.vue";

// export function subMenu_(subMenu) {
//   const arr = [];
//   if (subMenu && subMenu.length > 0) {
//     for (let i = 0; i < subMenu.length; i++) {
//       const obj = {};
//       obj.name = subMenu[i].name;
//       obj.path = subMenu[i].path;
//       obj.redirect = subMenu[i].redirect;
//       obj.meta = subMenu[i].meta;
//       // 处理组件
//       if (subMenu[i].component === "Layout") {
//         obj.component = Layout;
//       } else {
//         const component = subMenu[i].component;
//         obj.component = (resolve) => require([`@/views/${component}`], resolve);
//       }
//       // 子路由
//       obj.children = subMenu(subMenu[i].children); // 这里存在递归调用问题，subMenu函数还未定义完就被调用，会导致错误
//       arr.push(obj);
//     }
//   }
//   return arr;
// }
