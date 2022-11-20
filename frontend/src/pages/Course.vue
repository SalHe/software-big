<template>
  <div id="courseRoot">
    <div id="courseHeader">
      <el-button size="small" type="success" id="goHomeBtn" @click="goToHome"
        style="margin-left: 10px; margin-top: 10px">
        返回
      </el-button>
      <el-divider direction="vertical"></el-divider>
      <el-input placeholder="请输入课程名" prefix-icon="el-icon-search" v-model="seacherClassWord"
        style="width: 200px; margin-top: 10px; margin-left: 10px">
      </el-input>
      <el-button size="small" type="primary" id="searchPaperBtn" @click="searchClass"
        style="margin-left: 10px; margin-top: 10px">
        搜索
      </el-button>
      <el-button v-if="isTeacher" type="primary" class="courseHeaderBtn" style="margin-top: 10px" size="small"
        @click="createCourse">
        创建班级
      </el-button>
    </div>
    <el-divider content-position="left" id="courseClassDiv">
      班级列表
    </el-divider>
    <div id="courseClassInfoBox">
      <el-table :data="classes" style="width: 100%; border-radius: 25px" border stripe>
        <el-table-column prop="classId" label="课程号"> </el-table-column>
        <el-table-column prop="className" label="课程名"> </el-table-column>
        <el-table-column prop="owner" label="教师ID" v-if="!isTeacher">
        </el-table-column>
        <el-table-column>
          <template slot-scope="scope">
            <el-button type="success" v-if="isTeacher" @click="searchClassStudent(scope.$index)">
              查看学生
            </el-button>
            <el-button type="success" v-if="isTeacher" @click="addStudentToClass(scope.$index)">
              添加学生
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-dialog title="创建班级" :visible.sync="dialogFormCreateCourse">
      <el-form :model="form">
        <el-form-item label="班级名称" :label-width="formLabelWidth">
          <el-input v-model="className" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createCourse">取 消</el-button>
        <el-button type="primary" @click="onSubmitCourse">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="添加学生" :visible.sync="dialogFormCreateJoin">
      <el-form :model="form">
        <el-form-item label="学生ID" :label-width="formLabelWidth">
          <el-input v-model="joinClassStuId" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="joinClass">取 消</el-button>
        <el-button type="primary" @click="onSubmitJoin">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="查看学生" :visible.sync="dialogFormStuList">
      <el-table :data="stuList" style="width: 100%" border stripe>
        <el-table-column prop="id" label="学生ID"> </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="stuListCancel">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import {
  GetCourseCreated,
  PostCourse,
  PostCourseJoined,
  PostCourseSearch,
  GetCourseStu,
} from "../request/api";
export default {
  name: "Course",
  data() {
    return {
      seacherClassWord: "",
      classes: [],
      dialogFormCreateCourse: false,
      dialogFormCreateJoin: false,
      classPsw: "",
      className: "",
      joinClassStuId: -1,
      joinClassIndex: -1,
      stuList: [],
      dialogFormStuList: false,
    };
  },
  computed: {
    isTeacher() {
      return sessionStorage.getItem("role") == "教师";
    },
  },
  methods: {
    searchClass() {
      PostCourseSearch("?page=1&limit=20", {
        className: this.seacherClassWord,
      }).then((res) => {
        console.log(res);
        this.classes = res.data.data;
      });
    },
    goToHome() {
      this.$router.push({ path: "/" });
    },
    createCourse() {
      this.dialogFormCreateCourse = !this.dialogFormCreateCourse;
    },
    onSubmitCourse() {
      if (this.className == "") {
        this.$message.error("格式有误");
      } else {
        PostCourse({
          className: this.className,
        }).then(res => {
          console.log(res);
        });
      }
      window.location.href = "/course";
    },
    addStudentToClass(index) {
      this.joinClassIndex = index;
      this.dialogFormCreateJoin = !this.dialogFormCreateJoin;
    },
    onSubmitJoin() {
      PostCourseJoined({
        classId: this.classes[this.joinClassIndex].classId,
        studentId: this.joinClassStuId,
      }).then((res) => {

        console.log({
          classId: this.classes[this.joinClassIndex].classId,
          studentId:parseInt(this.joinClassStuId),
        });
        
        console.log(res);
        window.location.href = "/course";
      });
    },
    searchClassStudent(index) {
      GetCourseStu(this.classes[index].classId).then((res) => {
        this.stuList = [];
        for (var i = 0; i < res.data.data.length; i++) {
          var obj = {
            id: res.data.data[i],
          };
          this.stuList.push(obj);
        }
      });
      this.dialogFormStuList = true;
    },
    stuListCancel() {
      this.dialogFormStuList = !this.dialogFormStuList;
    },
    joinClass() {
      this.dialogFormCreateJoin = false;
    }
  },
  mounted() {
    if (sessionStorage.getItem("role") == "教师") {
      GetCourseCreated().then((res) => {
        this.classes = res.data.data;
      });
    }
  },
};
</script>

<style>
#courseRoot {
  position: relative;
  height: 753px;
}

#courseHeader {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 15px;
  height: 55px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}

#courseClassInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 115px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  border-radius: 25px;
}

#courseClassDiv {
  top: 95px;
  margin-left: 5%;
  width: 95%;
}
</style>