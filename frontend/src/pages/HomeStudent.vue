<template>
  <div id="homeRoot">
    <div id="userInfoBox">
      <el-avatar :size="90" :src="circleUrl" id="userAvatar"></el-avatar>
      <el-descriptions title="用户信息" id="userDescription">
        <el-descriptions-item label="用户ID" class="userInfo">
          {{ userId }}
        </el-descriptions-item>
        <el-descriptions-item label="学校/机构" class="userInfo">
          {{instituteName}}
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <el-divider content-position="left" id="studentExamDiv">
      所有评教
    </el-divider>
    <div id="examInfoBox">
      <el-table :data="exams" style="width: 100%; border-radius: 25px" border stripe height="243">
        <el-table-column prop="className" label="课程名" width=".25">
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width=".25">
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width=".25">
        </el-table-column>
        <el-table-column prop="problemSetName" label="评教名称" width=".25">
        </el-table-column>
        <el-table-column prop="time" label="" width=".25">
          <template slot-scope="scope">
            <el-button type="success" @click="enterExam(scope.$index)">
              进入评教
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-divider content-position="left" id="studentClassDiv">
      班级列表
    </el-divider>
    <div id="studentClassInfoBox">
      <el-table :data="classes" style="width: 100%; border-radius: 25px" border stripe height="200">
        <el-table-column prop="classId" label="课程号" width=".33">
        </el-table-column>
        <el-table-column prop="className" label="课程名" width=".33">
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import { GetCourseJoined, GetUserInfo, GetExamJoined } from "../request/api.js";
export default {
  name: "HomeStudent",
  data() {
    return {
      circleUrl:
        "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
      userId: 0,
      instituteName: "",
      exams: [],
      classes: [],
    };
  },
  components: {},
  mounted() {
    GetUserInfo().then((res) => {
      this.userId = res.data.userId;
      this.instituteName = res.data.instituteName;
    });
    GetCourseJoined().then((res) => {
      this.classes = res.data.data;
      for (let i = 0; i < this.classes.length; i++) {
        GetExamJoined({
          classId: this.classes[i].classId,
        }).then((res) => {
          for (let i = 0; i < res.data.data.length; i++) {
            this.exams.push(res.data.data[i]);
          }
        });
      }
    });
  },
  methods: {
    enterExam(index) {
      // TODO
      // 校验时间是否合法
      this.$router.push({
        name: "Exam",
        params: {
          problemSetId: this.exams[index].problemSetId,
          classId: this.exams[index].classId,
          startTime: this.exams[index].startTime,
          endTime: this.exams[index].endTime,
          className: this.exams[index].className,
          problemSetName: this.exams[index].problemSetName,
          studentId: this.userId,
        },
      });
    },
  },
};
</script>

<style>
#homeRoot {
  position: relative;
  height: 753px;
}

#userInfoBox {
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

#examInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 165px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}

.examInfoItem {
  font-size: 20px;
  margin-left: 40px;
  margin-top: 10px;
  color: grey;
}

#studentExamDiv {
  margin-left: 5%;
  top: 145px;
  width: 95%;
}

#studentClassDiv {
  margin-left: 5%;
  top: 420px;
  width: 95%;
}

#studentClassInfoBox {
  user-select: none;
  position: absolute;
  left: 5%;
  top: 465px;
  width: 90%;
  border: solid 1px rgb(207, 206, 206);
  background-color: white;
  border-radius: 25px;
}

#faceOuter {
  height: 600px;
}
</style>