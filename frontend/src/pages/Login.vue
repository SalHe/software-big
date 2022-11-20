<template>
  <div id="loginRoot">
    <div id="loginBox">
      <el-form v-model="login" id="loginForm">
        <span id="accountChar"><i class="el-icon-user-solid"></i> </span>
        <el-input
          class="input"
          id="accountInput"
          v-model="login.account"
          placeholder="请输入账号"
        >
        </el-input>
        <span id="passwordChar"><i class="el-icon-lock"></i></span>
        <el-input
          class="input"
          id="passwordInput"
          v-model="login.password"
          placeholder="请输入密码"
          show-password
        >
        </el-input>
      </el-form>
      <el-button id="loginSubBtn" type="primary" @click="onSubmit">
        登录
      </el-button>
      <el-link id="charLink" type="info" @click="goToRegister"
        >点此注册</el-link
      >
    </div>
  </div>
</template>

<script>
import { Login, GetUserInfo } from "../request/api.js";
export default {
  name: "Login",
  data() {
    return {
      loginStatus: 0,
      login: {
        account: "",
        password: "",
      },
    };
  },
  methods: {
    onSubmit() {
      if (
        this.login.password.length < 6
      ) {
        this.$message.error("密码小于6位");
      } else {
        Login({
          username: this.login.account,
          password: this.login.password,
        }).then((res) => {
          if (res.data.code > 0) {
            this.$message.error("用户名或密码错误");
          } else {
            sessionStorage.setItem("token", res.data.token);
            GetUserInfo().then((res) => {
              sessionStorage.setItem("role", res.data.role);
              window.location.href = "/";
            });
          }
        });
      }
    },
    goToRegister() {
      this.$router.push({ path: "/register" });
    },
  },
};
</script>

<style>
#loginRoot {
  position: relative;
    height: 691px;
}
#loginBox {
  width: 400px;
  height: 250px;
  border: solid rgb(207, 206, 206) 1px;
  border-radius: 30px;
  user-select: none;
  position: absolute;
  left: 50%;
  margin-left: -200px;
  margin-top: 150px;
  background-color: white;
}
.input {
  width: 270px;
}
#accountInput {
  position: absolute;
  top: 30px;
  left: 100px;
}
#passwordInput {
  position: absolute;
  top: 60px;
  left: 100px;
}
#accountChar {
  color: gray;
  position: absolute;
  font-size: 30px;
  top: 47px;
  left: 32px;
}
#passwordChar {
  color: gray;
  position: absolute;
  font-size: 30px;
  top: 107px;
  left: 32px;
}
#loginSubBtn {
  position: absolute;
  left: 160px;
  top: 170px;
}
#charLink {
  position: absolute;
  left: 165px;
  top: 215px;
}
#charLink:hover {
  color: brown;
}
</style>