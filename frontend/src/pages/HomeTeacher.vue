<template>
  <div id="homeTeacherRoot">
    <div id="teacherInfoBox">
      <el-avatar :size="90" :src="circleUrl" id="userAvatar"></el-avatar>
      <el-descriptions title="用户信息" id="userDescription">
        <el-descriptions-item label="学校/机构" class="uerInfo">{{
          instituteName
        }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <el-divider content-position="left" id="teacherManageDiv">
      教务管理
    </el-divider>
    <div id="teacherManageBox">
      <el-button id="bankBtn" type="success" @click="goToQuestionBank">
        评教题目列表
      </el-button>
      <el-button id="examManageBtn" type="success" @click="goToExamManage">
        评教管理
      </el-button>
      <el-button id="courseBtn" type="success" @click="goToCourse">
        班级管理
      </el-button>
      <el-button id="addStuBtn" type="primary" @click="showDialogAddStu">
        为机构添加学生
      </el-button>
    </div>
    <el-divider content-position="left" id="teacherClassDiv">
      班级列表
    </el-divider>
    <div id="teacherClassInfoBox">
      <el-table
        :data="classes"
        style="width: 100%; border-radius: 25px"
        border
        stripe
        height="360"
      >
        <el-table-column prop="classId" label="课程号" width=".33">
        </el-table-column>
        <el-table-column prop="className" label="课程名" width=".33">
        </el-table-column>
        <!-- <el-table-column width=".33" label="成绩管理">
          <template slot-scope="scope">
            <el-button type="success" @click="setOffGrade(scope.$index)">
              查看评价分布
            </el-button>
            <el-button type="success" @click="showCloud(scope.$index)">
              查看评价词云
            </el-button>
          </template>
        </el-table-column> -->
      </el-table>
    </div>
    <el-dialog title="评价词云" :visible.sync="offWeightForm">
      <el-image :src="imgSrc" style="width: 60%"></el-image>
      <div slot="footer" class="dialog-footer">
        <el-button @click="offWeightForm = false">取 消</el-button>
        <el-button type="primary" @click="subOffWeight">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="评价分布" :visible.sync="offGradeForm">
      <Echarts />
      <div slot="footer" class="dialog-footer">
        <el-button @click="offGradeForm = false">取 消</el-button>
        <el-button type="primary" @click="subOffGrade">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="为机构添加学生" :visible.sync="dialogFormAddStu">
      <el-form :model="form">
        <el-form-item label="学生姓名" :label-width="formLabelWidth">
          <el-input v-model="student.studentName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="学生学号" :label-width="formLabelWidth">
          <el-input v-model="student.studentNum" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="学生密码" :label-width="formLabelWidth">
          <el-input
            v-model="student.password"
            autocomplete="off"
            show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancelAddStu">取 消</el-button>
        <el-button type="primary" @click="addStu">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import Echarts from "./Echarts.vue";
import {
  GetCourseCreated,
  GetUserInfo,
  PostStudent,
  GetCloudPic,
} from "../request/api.js";
export default {
  name: "HomeTeacher",
  data() {
    return {
      circleUrl:
        "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
      userId: 0,
      instituteName: "",
      classes: [],
      offWeightForm: false,
      postOffWightIndex: -1,
      offWeight: 0,
      offGradeForm: false,
      stuIdList: [],
      postOffGradeIndex: -1,
      student: {
        studentNum: "",
        studentName: "",
        password: "",
      },
      dialogFormAddStu: false,
      imgSrc:""
    };
  },
  components: {
    Echarts,
  },
  methods: {
    goToQuestionBank() {
      this.$router.push({ name: "QuestionBank" });
    },
    goToCourse() {
      this.$router.push({ name: "Course" });
    },
    goToExamManage() {
      this.$router.push({ name: "ExamManage" });
    },
    showCloud(index) {
      GetCloudPic()
      this.offWeightForm = true;
      this.postOffWightIndex = index;
    },
    cancelAddStu() {
      this.dialogFormAddStu = false;
    },
    showDialogAddStu() {
      this.dialogFormAddStu = true;
    },
    addStu() {
      PostStudent(this.student).then((res) => {
        console.log(res);
      });

      this.dialogFormAddStu = false;
      this.student.account = "";
      this.student.password = "";
      this.student.name = "";
      this.$router.push({ path: "/" });
      this.$message("添加成功");
    },
  },
  mounted() {
    GetUserInfo().then((res) => {
      this.userId = res.data.userId;
      this.instituteName = res.data.instituteName;
    });
    GetCourseCreated().then((res) => {
      this.classes = res.data.data;
    });
  },
};
</script>

<style>
#homeRoot {
  position: relative;
  height: 753px;
}
#teacherInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 15px;
  height: 100px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}
.userInfo {
  position: absolute;
}
#userDescription {
  position: absolute;
  left: 120px;
  width: 600px;
}
#userAvatar {
  position: absolute;
  left: 12px;
  top: 5px;
}
#userDescription {
  position: absolute;
  left: 114px;
  top: 15px;
}
#teacherManageBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 155px;
  height: 100px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}
#bankBtn {
  position: absolute;
  left: 15px;
  top: 35px;
}
/* #reviewBtn {
  position: absolute;
  left: 125px;
  top: 35px;
} */
#courseBtn {
  position: absolute;
  left: 265px;
  top: 35px;
}
#examManageBtn {
  position: absolute;
  left: 145px;
  top: 35px;
}
#addStuBtn {
  position: absolute;
  left: 385px;
  top: 35px;
}
#teacherClassInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 295px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}
#teacherClassDiv {
  top: 255px;
  margin-left: 5%;
  width: 95%;
}
#teacherManageDiv {
  top: 140px;
  margin-left: 5%;
  width: 95%;
}
</style>