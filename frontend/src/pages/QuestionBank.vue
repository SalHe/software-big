<template>
  <div id="questionBankRoot">
    <div id="questionBox">
      <div id="questionHeader">
        <el-page-header
          @back="goBack"
          content="题库管理"
          id="questionPageHeader"
        >
        </el-page-header>
        <el-button
          size="small"
          type="primary"
          id="createPaperBtn"
          @click="switchProblemSet"
        >
          组卷
        </el-button>
        <el-input
          placeholder="请输入关键词"
          prefix-icon="el-icon-search"
          v-model="seacherWord"
          style="width: 200px; position: absolute; left: 70%; top: 10px"
        >
        </el-input>
        <el-button
          size="small"
          type="primary"
          id="searchPaperBtn"
          style="position: absolute; left: 85%; top: 15px"
          @click="searchQuestion"
        >
          搜索
        </el-button>
        <el-button
          size="small"
          type="success"
          id="searchPaperBtn"
          style="position: absolute; left: 89.5%; top: 15px"
          @click="goToProblem"
        >
          添加
        </el-button>
      </div>
      <el-divider id="questionDivider">所有题目</el-divider>
      <div id="questionBankBox">
        <el-form>
          <el-form-item
            v-for="item in questionPage"
            :key="item.problemId"
            :label="item.problemId + '.'"
            class="questionBankItem"
          >
            <el-image
              :src="imgSrc[item.problemId]"
              style="width: 40%"
            ></el-image>
            <br /><br />
            <el-radio-group v-if="item.type == '单选题'">
              <el-radio v-for="n in item.total" :key="n" :label="n" disabled>
                {{ comments[n - 1] }}
              </el-radio>
            </el-radio-group>
            <el-form-item v-if="item.type == '填空题'"> </el-form-item>
            <div v-if="createProblemSet">
              <br />
              <el-button
                type="warning"
                @click="addProblemToSet(item.problemId)"
              >
                添加该题
              </el-button>
            </div>
            <el-divider></el-divider>
          </el-form-item>
        </el-form>
      </div>
      <div id="questionPagination">
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-size="20"
          layout="total, prev, pager, next"
          :total="questionTotalNum"
          :hide-on-single-page="true"
          background
        >
        </el-pagination>
      </div>
      <el-drawer
        title="新的评教"
        :visible.sync="drawerShow"
        direction="rtl"
        size="50%"
      >
        <el-form>
          <el-form-item>
            <el-input
              v-model="problemSetName"
              placeholder="请输入评教名称"
            ></el-input>
          </el-form-item>
        </el-form>
        <el-table :data="problemSet" height="600">
          <el-table-column
            property="problemId"
            label="题目号"
          ></el-table-column>
          <!-- <el-table-column label="分值">
            <template slot-scope="scope">
              <el-input-number
                v-model="problemSet[scope.$index].score"
                :min="1"
                :max="100"
              ></el-input-number>
            </template>
          </el-table-column> -->
          <el-table-column>
            <template slot-scope="scope">
              <el-button
                type="danger"
                @click="deleteProblemFromList(scope.$index)"
                size="small"
                style="float: right"
              >
                删除该题
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button
          type="primary"
          @click="subProblemSet"
          style="float: right; margin: 10px"
        >
          完成
        </el-button>
      </el-drawer>
    </div>
    <div
      id="setDrawerTag"
      v-if="createProblemSet"
      @click="drawerShow = !drawerShow"
    >
      &nbsp;&nbsp;点击打开待整合评教题目列表
    </div>
  </div>
</template>

<script>
import {
  GetProblemId,
  GetProblemPic,
  PostProblemSet,
  PutProblemSet,
} from "../request/api";
export default {
  name: "QuestionBank",
  data() {
    return {
      fileList: [],
      questionTotalNum: 60,
      currentPage: 1,
      searchWord: "",
      createProblemSet: false,
      drawerShow: false,
      problemSetName: "",
      questionPage: [],
      problemSet: [],
      imgSrc: [],
      comments: ["优秀", "良好", "及格", "不合格"],
    };
  },
  methods: {
    goBack() {
      this.$router.push({ path: "/" });
    },
    handleExceed() {
      this.$message.warning("超出文件上传的限制数量");
    },
    handleCurrentChange() {
      GetProblemId("?page=" + this.currentPage + "&" + "limit=20").then(
        (res) => {
          this.questionPage = res.data.data;
          for (var i = 0; i < this.questionPage.length; i++) {
            GetProblemPic(this.questionPage[i].problemId).then((res) => {
              this.createImgSrc(res.data, res.config.url.slice(9));
            });
          }
        }
      );
    },
    goToProblem() {
      this.$router.push({ name: "Problem" });
    },
    switchProblemSet() {
      this.createProblemSet = !this.createProblemSet;
    },
    addProblemToSet(problemId) {
      var obj = {
        problemId: problemId
      };
      this.problemSet.push(obj);
    },
    deleteProblemFromList(index) {
      this.problemSet.splice(index, 1);
    },
    subProblemSet() {
      if (this.problemSet.length == 0 || this.problemSetName == "") {
        this.$message("试卷名与题库均不能为空");
      } else {
        var problemSetId;
        PostProblemSet({
          problemSetName: this.problemSetName,
        }).then((res) => {
          problemSetId = res.data.problemSetId;
          console.log(this.problemSet);
          console.log(problemSetId);
          PutProblemSet({
            problemSetId,
            problemList: this.problemSet,
          }).then((res) => {
            this.$router.go(0);
            console.log(res);
          });
        });
      }
    },
    createImgSrc(blob, problemId) {
      // 浏览器允许使用URL.createObjectURL()方法，针对 Blob 对象生成一个临时 URL。
      // 这个 URL 以blob://开头,表明对应一个 Blob 对象。
      this.imgSrc[problemId] = window.URL.createObjectURL(blob);
      this.imgSrc.splice(0, 0);
    },
  },
  mounted() {
    GetProblemId("?page=1&limit=20").then((res) => {
      this.questionPage = res.data.data;
      for (var i = 0; i < this.questionPage.length; i++) {
        GetProblemPic(this.questionPage[i].problemId).then((res) => {
          this.createImgSrc(res.data, res.config.url.slice(9));
        });
      }
    });
  },
};
</script>

<style>
#questionBankRoot {
  position: relative;
}
#questionBox {
  position: absolute;
  border: 1px solid grey;
  left: 5%;
  width: 90%;
  top: 50px;
  user-select: none;
}
#questionHeader {
  position: relative;
  width: 100%;
  height: 50px;
}
#questionPageHeader {
  position: absolute;
  left: 10px;
  top: 20px;
}
#questionList {
  width: 100%;
  height: 300px;
}
#questionPagination {
  position: relative;
}
.el-pagination {
  text-align: center;
}
.questionBankItem {
  margin-left: 15px;
}
#createPaperBtn {
  margin-left: 95%;
  margin-top: 15px;
}
#setDrawerTag {
  writing-mode: vertical-lr;
  width: 22px;
  height: 150px;
  font-size: 15px;
  color: gray;
  user-select: none;
  float: right;
  border: 2px solid rgb(216, 216, 216);
  margin-top: 15px;
  border-radius: 10px;
}
#setDrawerTag:hover {
  color: rgb(112, 169, 255);
  border: 2px solid rgb(190, 216, 255);
}
</style>