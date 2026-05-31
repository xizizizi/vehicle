<template>
    <div class="tech-breadcrumb-container">
      <el-breadcrumb class="tech-breadcrumb" separator="">
        <transition-group name="tech-breadcrumb">
          <el-breadcrumb-item 
            v-for="(item,index) in levelList" 
            :key="item.path"
            class="breadcrumb-item"
            :class="{ 'active': index === levelList.length - 1 }"
          >
            <div class="breadcrumb-content">
              <span 
                v-if="item.redirect==='noRedirect'||index==levelList.length-1" 
                class="no-redirect"
              >
                {{ item.meta.title }}
              </span>
              <a v-else @click.prevent="handleLink(item)" class="redirect-link">
                {{ item.meta.title }}
              </a>
              <div class="tech-divider" v-if="index < levelList.length - 1">
                <div class="divider-line"></div>
                <div class="divider-arrow">▶</div>
              </div>
            </div>
          </el-breadcrumb-item>
        </transition-group>
      </el-breadcrumb>
    </div>
  </template>
  
  <script>
  import pathToRegexp from 'path-to-regexp'
  
  export default {
    data() {
      return {
        levelList: null
      }
    },
    watch: {
      $route() {
        this.getBreadcrumb()
      }
    },
    created() {
      this.getBreadcrumb()
    },
    methods: {
      getBreadcrumb() {
        // only show routes with meta.title
        let matched = this.$route.matched.filter(item => item.meta && item.meta.title)
        const first = matched[0]
  
        if (!this.isDashboard(first)) {
          matched = [{ path: '/dashboard', meta: { title: '首页' }}].concat(matched)
        }
  
        this.levelList = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
      },
      isDashboard(route) {
        const name = route && route.name
        if (!name) {
          return false
        }
        return name.trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
      },
      pathCompile(path) {
        // To solve this problem https://github.com/PanJiaChen/vue-element-admin/issues/561
        const { params } = this.$route
        var toPath = pathToRegexp.compile(path)
        return toPath(params)
      },
      handleLink(item) {
        const { redirect, path } = item
        if (redirect) {
          this.$router.push(redirect)
          return
        }
        this.$router.push(this.pathCompile(path))
      }
    }
  }
  </script>
  
  <style lang="scss" scoped>
  .tech-breadcrumb-container {
    background: linear-gradient(90deg, rgba(10, 25, 47, 0.9) 0%, rgba(16, 36, 70, 0.8) 100%);
    border: 1px solid rgba(64, 158, 255, 0.3);
    box-shadow: 0 0 15px rgba(64, 158, 255, 0.3), 
                inset 0 0 10px rgba(64, 158, 255, 0.2);
    border-radius: 4px;
    padding: 0 15px;
    margin: 10px 0;
    position: relative;
    overflow: hidden;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.7), transparent);
      animation: scanline 3s linear infinite;
    }
  }
  
  .tech-breadcrumb {
    display: flex;
    align-items: center;
    height: 46px;
    padding: 0;
    margin: 0;
    font-family: 'Arial', sans-serif;
    
    .breadcrumb-item {
      display: flex;
      align-items: center;
      height: 100%;
      padding: 0 15px;
      position: relative;
      
      &:not(:last-child)::after {
        display: none;
      }
      
      &.active {
        background: rgba(64, 158, 255, 0.15);
        box-shadow: 0 0 10px rgba(64, 158, 255, 0.3);
        
        .no-redirect {
          color: #fff;
          text-shadow: 0 0 8px rgba(64, 158, 255, 0.8);
        }
      }
      
      .breadcrumb-content {
        display: flex;
        align-items: center;
      }
      
      .no-redirect {
        color: #97a8be;
        font-weight: 500;
        font-size: 15px;
        letter-spacing: 0.5px;
        transition: all 0.3s;
      }
      
      .redirect-link {
        color: #4dabf7;
        font-weight: 500;
        font-size: 15px;
        text-decoration: none;
        letter-spacing: 0.5px;
        cursor: pointer;
        transition: all 0.3s;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          bottom: -3px;
          left: 0;
          width: 0;
          height: 1px;
          background: #4dabf7;
          transition: width 0.3s;
        }
        
        &:hover {
          color: #64b5f6;
          text-shadow: 0 0 8px rgba(100, 181, 246, 0.6);
          
          &::after {
            width: 100%;
          }
        }
      }
    }
    
    .tech-divider {
      display: flex;
      align-items: center;
      margin-left: 20px;
      
      .divider-line {
        width: 20px;
        height: 1px;
        background: rgba(64, 158, 255, 0.5);
        position: relative;
        
        &::before {
          content: '';
          position: absolute;
          top: -2px;
          right: 0;
          width: 5px;
          height: 5px;
          background: rgba(64, 158, 255, 0.8);
          border-radius: 50%;
          animation: pulse 1.5s infinite;
        }
      }
      
      .divider-arrow {
        color: rgba(64, 158, 255, 0.7);
        font-size: 10px;
        margin-left: 5px;
        transform: scale(0.8);
      }
    }
  }
  
  .tech-breadcrumb-enter-active,
  .tech-breadcrumb-leave-active {
    transition: all 0.5s;
  }
  
  .tech-breadcrumb-enter,
  .tech-breadcrumb-leave-active {
    opacity: 0;
    transform: translateX(20px);
  }
  
  .tech-breadcrumb-leave-active {
    position: absolute;
  }
  
  @keyframes scanline {
    0% {
      transform: translateX(-100%);
    }
    100% {
      transform: translateX(100%);
    }
  }
  
  @keyframes pulse {
    0% {
      box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.7);
    }
    70% {
      box-shadow: 0 0 0 5px rgba(64, 158, 255, 0);
    }
    100% {
      box-shadow: 0 0 0 0 rgba(64, 158, 255, 0);
    }
  }
  </style>