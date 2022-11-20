<template>
  <div id="reviewRoot">
    <div id="answerBox">
      <div id="questionHeader">
        <el-page-header
          @back="goBack"
          :content="className + '-' + problemSetName"
          id="questionPageHeader"
        >
        </el-page-header>
        <!-- <el-button
          size="small"
          type="primary"
          id="subGradeBtn"
          @click="subStudentGrades"
          style="margin-top: 15px; float: right; margin-right: 15px"
        >
          提交分数
        </el-button>
        <el-button
          size="small"
          type="warning"
          id="nextAnsSetBtn"
          @click="nextStuAns"
          style="margin-top: 15px; float: right; margin-right: 20px"
        >
          下一个
        </el-button>
        <el-button
          size="small"
          type="warning"
          id="preAnsSetBtn"
          @click="preStuAns"
          style="margin-top: 15px; float: right; margin-right: 5px"
        >
          上一个
        </el-button> -->
      </div>
      <el-divider id="questionDivider">阅卷区域</el-divider>
      <div id="questionBankBox">
        <el-form>
          <el-form-item
            v-for="(item, index) in questions"
            :key="item.problemId"
            :label="index + 1"
            class="questionBankItem"
          >
            <el-image
              :src="questionImgSrc[item.problemId]"
              style="width: 40%"
            ></el-image>
            <br /><br />
            <el-radio-group
              v-if="item.type == '单选题'"
              v-model="ansList[index]"
            >
              <el-radio v-for="n in item.total" :key="n" :label="n" disabled>
                {{ comments[n - 1] }}
              </el-radio>
            </el-radio-group>
            <el-form-item v-if="item.type == '单选题'">
              <el-button
                type="warning"
                style="margin-top: 15px"
                @click="showChoice(index)"
              >
                查看选项频率分布
              </el-button>
            </el-form-item>
            <el-form-item v-if="item.type == '填空题'">
              <el-button type="warning" @click="showCloud(index)">
                查看词云分布
              </el-button>
              <el-button type="warning" @click="showEmotion(index)">
                查看情感分布
              </el-button>
            </el-form-item>

            <el-divider></el-divider>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <el-dialog title="评价词云" :visible="dialogFormCloud">
      <el-image :src="imgSrc" style="width: 60%"></el-image>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormCloud = false">确定</el-button>
      </div>
    </el-dialog>
    <el-dialog :title="title + '分布'" :visible="dialogFormResult">
      <Echarts :proparray="proparray" ref="echart"></Echarts>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogFormResultCancel">
          确 定
        </el-button>
      </div>
    </el-dialog>
    <!-- <el-dialog title="选项分布" :visible.sync="dialogFormChoice">
      <Echarts :proparray="proparray" ref="echart"></Echarts>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogFormChoice = false">
          确 定
        </el-button>
      </div>
    </el-dialog> -->
  </div>
</template>

<script>
import Echarts from "./Echarts.vue";
import {
  PostExamContent,
  GetProblemPic,
  PostAnsList,
  PostSimpleAnsGet,
  GetAnsPic,
  GetCloudPic,
  PostEmotion,
  PostChoice,
} from "../request/api";
export default {
  name: "Review",
  data() {
    return {
      studentId: 76,
      className: "CSAPP2022",
      problemSetName: "CSAPPtest8",
      questions: [],
      questionImgSrc: [],
      ansImgSrc: [],
      ansSetList: [],
      ansList: [],
      stuIndex: 0,
      scoreList: [],
      comments: ["优秀", "良好", "及格", "不合格"],
      dialogFormCloud: false,
      dialogFormResult: false,
      imgSrc: "",
      proparray: [],
      emotions: ["最消极", "较消极", "中立", "较积极", "最积极"],
      title: "",
    };
  },
  components: {
    Echarts,
  },
  methods: {
    goBack() {
      this.$router.push({ name: "ExamManage" });
    },
    createImgSrc(blob, problemId) {
      // 浏览器允许使用URL.createObjectURL()方法，针对 Blob 对象生成一个临时 URL。
      // 这个 URL 以blob://开头,表明对应一个 Blob 对象。
      this.questionImgSrc[problemId] = window.URL.createObjectURL(blob);
      this.questionImgSrc.splice(0, 0);
    },
    getAns() {
      for (let i = 0; i < this.questions.length; i++) {
        if (this.questions[i].type == "单选题") {
          PostSimpleAnsGet({
            answerSetId: this.ansSetList[this.stuIndex].answerSetId,
            order: i,
          }).then((res) => {
            this.ansList[i] = res.data.answer;
          });
        } else {
          GetAnsPic(this.ansSetList[this.stuIndex].answerSetId + "/" + i).then(
            (res) => {
              this.ansList[i] = window.URL.createObjectURL(res.data);
            }
          );
          this.scoreList[i] = 0;
        }
      }
    },
    showCloud(index) {
      console.log("show cloud called");
      GetCloudPic(
        "?problemSetId=" +
          this.$route.params.problemSetId +
          "&classId=" +
          this.$route.params.classId +
          "&order=" +
          index
      ).then((res) => {
        console.log("get cloud called");
        console.log(res);
        this.createImgSrcReview(res.data);
        this.dialogFormCloud = true;
      });
      const loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
      });
      setTimeout(() => {
        loading.close();
      }, 1500);
      console.log("show cloud ended");
    },
    createImgSrcReview(blob) {
      // 浏览器允许使用URL.createObjectURL()方法，针对 Blob 对象生成一个临时 URL。
      // 这个 URL 以blob://开头,表明对应一个 Blob 对象。
      this.imgSrc = window.URL.createObjectURL(blob);
    },
    showEmotion(index) {
      console.log("show emotion called");
      this.title = "情感";
      PostEmotion({
        problemSetId: this.$route.params.problemSetId,
        classId: this.$route.params.classId,
        order: index,
      }).then((res) => {
        console.log("post emotion called");
        this.proparray = [];
        for (let index = 0; index < res.data.result.length; index++) {
          var obj = {
            value: res.data.result[index],
            name: this.emotions[index],
          };
          this.proparray.push(obj);
        }
        this.dialogFormResult = true;
        setTimeout(() => {
          this.$refs.echart.myEcharts();
        }, 100);
      });
      console.log("show emotion ended");
    },
    showChoice(index) {
      console.log("show choice called");
      this.title = "选项";
      PostChoice({
        problemSetId: this.$route.params.problemSetId,
        classId: this.$route.params.classId,
        order: index,
      }).then((res) => {
        console.log("post choice called");
        this.proparray = [];
        console.log(res);
        for (let index = 0; index < res.data.result.length; index++) {
          var obj = {
            value: res.data.result[index],
            name: this.comments[index],
          };
          this.proparray.push(obj);
        }
        console.log(this.proparray);
        this.dialogFormResult = true;
        setTimeout(() => {
          this.$refs.echart.myEcharts();
        }, 100);
      });
      console.log("show choice ended");
    },
    dialogFormResultCancel() {
      this.dialogFormResult = false;
      this.$refs.echart.dispose();
    },
  },
  mounted() {
    // 获取试卷
    PostExamContent({
      problemSetId: this.$route.params.problemSetId,
    }).then((res) => {
      this.questions = res.data.data;
      for (let i = 0; i < this.questions.length; i++) {
        GetProblemPic(this.questions[i].problemId).then((res) => {
          this.createImgSrc(res.data, res.config.url.slice(9));
        });
      }
      // 获取全班答题卡
      PostAnsList({
        problemSetId: this.$route.params.problemSetId,
        classId: this.$route.params.classId,
      }).then((res) => {
        if (res.data.data.length == 0) {
          this.$message("当前暂无学生提交答卷");
          // this.$router.push({ name: "ExamManage" });
          console.log(res);
        } else {
          this.ansSetList = res.data.data;
          // this.getAns();
        }
      });
    });
  },
};
</script>

<style>
#reviewRoot {
  position: relative;
}
#answerBox {
  position: absolute;
  border: 1px solid grey;
  left: 5%;
  width: 90%;
  top: 50px;
  user-select: none;
}
</style>