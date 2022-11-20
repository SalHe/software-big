<template>
  <div id="problemRoot">
    <el-divider>上传题目</el-divider>
    <el-form id="createProblemForm">
      <div id="creteProblemBox">
        <el-form-item label="题型选择:">
          <el-radio-group id="problemTypeRadio" v-model="problemType">
            <el-radio :label="'单选题'">选择型</el-radio>
            <el-radio :label="'填空题'">填空型</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="上传题干:">
          <el-upload
            class="uploadQuestion"
            action=""
            :file-list="fileList"
            list-type="picture"
            :auto-upload="false"
            :limit="1"
            :on-exceed="handleExceed"
            accept=".png"
            ref="uploadProblem"
            :http-request="uploadImg"
          >
            <el-button size="small" type="primary">点击上传</el-button>
            <div slot="tip" class="el-upload__tip">
              只能上传1个jpg文件,且不超过500kb
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="选择选项总数:" v-if="problemType == '单选题'">
          <el-input-number
            v-model="optionNum"
            :min="1"
            :max="4"
          ></el-input-number>
        </el-form-item>
        
        <el-button
          id="subProblemBtn"
          @click="subProblem"
          type="primary"
          style="float: right; margin: 10px"
        >
          确定上传
        </el-button>
        <el-button
          id="goToQuestionBankBtn"
          @click="goToQuestionBank"
          type="success"
          style="float: right; margin: 10px"
        >
          返回
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { PostProblem } from "../request/api.js";
export default {
  name: "Problem",
  data() {
    return {
      problemType: "单选题",
      optionNum: 1,
      ansIndex: 1,
      img: "",
      fileList: [],
    };
  },
  methods: {
    handleExceed() {
      this.$message.warning("超出文件上传的限制数量");
    },
    goToQuestionBank() {
      this.$router.push({ name: "QuestionBank" });
    },
    subProblem() {
      if (this.problemType == "单选题") {
        this.$refs.uploadProblem.submit();
        var form = new FormData();
        form.append("type", this.problemType);
        form.append("answer", this.ansIndex);
        form.append("total", this.optionNum);
        form.append("image", this.img);
        console.log(this.img);
        PostProblem(form)
          .then((res) => {
            console.log(res);
          })
          .catch((err) => {
            console.log(err);
          });
      } else {
        this.$refs.uploadProblem.submit();
        var form_ = new FormData();
        form_.append("type", this.problemType);
        form_.append("image", this.img);
        PostProblem(form_)
          .then((res) => {
            console.log(res);
          })
          .catch((err) => {
            console.log(err);
          });
      }
      window.location.href="/";
    },
    uploadImg(p) {
      this.img = p.file;
    },
  },
};
</script>

<style>
#problemRoot {
  position: relative;
    height: 753px;
}
#createProblemForm {
  position: absolute;
  width: 90%;
  left: 5%;
  top: 55px;
  border: 1px solid gray;
    background-color: white;
}
#creteProblemBox {
  margin-top: 5px;
  margin-left: 15px;
  margin-bottom: 20px;

}
</style>