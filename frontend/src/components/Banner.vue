<template>
  <div>
    <el-menu
      :default-active="activeIndex2"
      class="el-menu-demo"
      mode="horizontal"
      background-color="#b9dbf8"
      text-color="#fff"
    >
      <el-menu-item id="homeBtn" @click="goToHome">首页</el-menu-item>
      <router-link to="/login" v-if="!isAuth">
        <el-menu-item id="loginBtn">登陆</el-menu-item>
      </router-link>
      <router-link to="/register" v-if="!isAuth">
        <el-menu-item id="registerBtn">注册</el-menu-item>
      </router-link>
      <el-menu-item id="logoutBtn" v-if="isAuth" @click="Logout">
        登出
      </el-menu-item>
      <el-submenu v-if="false">
        <template slot="title">设置</template>
        <el-menu-item @click="goToFaceImg">上传人脸识别基准图</el-menu-item>
      </el-submenu>
      <el-menu-item
        id="goToCourseBtn"
        @click="goToCourse"
        v-if="isAuth && !isAdmin && !isStu"
      >
        班级管理
      </el-menu-item>
      <el-submenu
        id="goToCourseBtn"
        @click="goToCourse"
        v-if="isAuth && isAdmin"
      >
        <template slot="title">模拟登陆</template>
        <el-menu-item @click="changeRole('教师')">模拟老师</el-menu-item>
        <el-menu-item @click="changeRole('学生')">模拟学生</el-menu-item>
        <el-menu-item @click="changeRole('管理员')">返回管理员</el-menu-item>
      </el-submenu>
      <el-menu-item
        id="userNewsBtn"
        @click="goToNews"
        v-if="isAuth"
        style="user-select: none"
      >
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  name: "Banner",
  data() {
    return {
      newsNum: 7,
    };
  },
  methods: {
    goToHome() {
      this.$router.push({ path: "/" });
    },
    goToCourse() {
      this.$router.push({ name: "Course" });
    },
    Logout() {
      sessionStorage.removeItem("token");
      sessionStorage.removeItem("role");
      window.location.href = "/";
    },
    goToFaceImg() {
      this.$router.push({
        name: "FaceImg",
        params: {
          notAsChild: true,
        },
      });
    },
    changeRole(str) {
      if (str == "教师" || str == "学生") {
        sessionStorage.setItem("role", str);
        sessionStorage.setItem("simulation", "true");
      } else {
        sessionStorage.setItem("role", str);
        sessionStorage.removeItem("simulation");
      }
      this.$router.push({ path: "/" });
    },
  },
  computed: {
    isAuth() {
      if (sessionStorage.getItem("token")) {
        return true;
      } else {
        return false;
      }
    },
    isStu() {
      if (sessionStorage.getItem("role") == "学生") {
        return true;
      } else {
        return false;
      }
    },
    isAdmin() {
      return (
        sessionStorage.getItem("role") == "管理员" ||
        sessionStorage.getItem("simulation")
      );
    },
  },
};
</script>

<style>
#loginBtn {
  user-select: none;
  float: right;
}
#registerBtn {
  user-select: none;
  float: right;
}
#homeBtn {
  user-select: none;
}
#logoutBtn {
  user-select: none;
  float: right;
}
#goToCourseBtn {
  user-select: none;
}
</style>