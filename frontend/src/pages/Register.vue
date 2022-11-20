<template>
  <div id="registerRoot">
    <div id="registerBox">
      <el-form v-model="register" id="registerForm">
        <span id="accountChar"><i class="el-icon-user-solid"></i> </span>
        <el-input
          class="input"
          id="accountInput"
          v-model="register.account"
          placeholder="请输入账号"
        >
        </el-input>
        <span id="passwordChar"><i class="el-icon-lock"></i></span>
        <el-input
          class="input"
          id="passwordInput"
          v-model="register.password"
          placeholder="请输入密码"
          show-password
        >
        </el-input>

        <span id="instituteChar"><i class="el-icon-school"></i></span>
        <el-input
          class="input"
          id="intituteInput"
          v-model="register.instituteName"
          placeholder="请输入机构名"
        >
        </el-input>
      </el-form>
      <el-button id="registerSubBtn" type="primary" @click="onSubmit">
        注册
      </el-button>
    </div>
  </div>
</template>

<script>
import { Register,  Login, GetUserInfo } from "../request/api.js";
export default {
  name: "Register",
  data() {
    return {
      idCode: 0,
      instituteId: 1,
      register: {
        account: "",
        password: "",
        instituteName:""
      },
      institutes: [],
    };
  },
  methods: {
    onSubmit() {
      console.log("注册请求");
      if (
        this.register.password.length < 6 ||
        this.register.account.length < 6 ||
        this.instituteCode == ""
      ) {
        this.$message.error("密码或账号小于6位 或 未选择注册机构");
      } else {
        Register({
          username: this.register.account,
          password: this.register.password,
          instituteName: this.register.instituteName,
        }).then((res) => {
          console.log(res);
          Login({
            username: this.register.account,
            password: this.register.password,
          }).then((res) => {
            sessionStorage.setItem("token", res.data.token);
            GetUserInfo().then((res) => {
              sessionStorage.setItem("role", res.data.role);
              window.location.href = "/";
            });
          });
        });
      }
    },
  },
  mounted() {

  },
};
</script>

<style>
#registerRoot {
  position: relative;
    height: 691px;
}
#registerBox {
  width: 400px;
  height: 290px;
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
#registerSubBtn {
  position: absolute;
  left: 160px;
  top: 220px;
}
#idRadio {
  position: absolute;
  left: 100px;
  top: 180px;
}
#idChar {
  color: gray;
  position: absolute;
  font-size: 30px;
  top: 167px;
  left: 32px;
}
.el-select-dropdown.el-popper.register {
  position: absolute !important;
  top: 417px !important;
  left: 50% !important;
  margin-left: -100px;
}
span.el-input__suffix {
  display: none;
}
#intituteInput {
  position: absolute;
  left: 100px;
  top: 90px;
}
#instituteChar {
  color: gray;
  position: absolute;
  font-size: 30px;
  top: 162px;
  left: 32px;
}
</style>