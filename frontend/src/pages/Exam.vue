<template>
  <div id="examRoot">
    <div id="examBox">
      <div id="examHeader">
        <el-page-header @back="goBack" :content="title" id="examPageHeader">
        </el-page-header>
      </div>
      <el-divider id="examDivider">答题区域</el-divider>
      <div id="examQuestionBox">
        <el-form>
          <el-form-item v-for="(item, index) in questions" :key="item.problemId" :label="index + 1 + '. '"
            class="examQuestionItem">
            <br />
            <el-image :src="imgSrc[item.problemId]" style="width: 60%"></el-image>
            <br />
            <el-radio-group v-if="item.type == '单选题'" v-model="ansList[index].answer">
              <el-radio v-for="n in item.total" :key="n" :label="n">
                {{ comments[n-1] }}
              </el-radio>
            </el-radio-group>
            <el-form-item v-if="item.type == '填空题'">
              <!-- <el-upload
                class="uploadAnsBtn"
                action=""
                multiple
                :limit="1"
                :on-exceed="handleExceed"
                :file-list="fileList"
                :auto-upload="false"
                ref="uploadAns"
                accept=".png"
                :http-request="uploadAnsImg"
                :data="index"
              >
                <el-button size="small" type="primary" v-if="cheat"
                  >点击上传</el-button
                >
                <div slot="tip" class="el-upload__tip">
                  只能上传png文件(最多1个)
                </div>
              </el-upload> -->
              <el-input type="textarea" placeholder="请输入内容" v-model="ansList[index].answer" maxlength="500"
                show-word-limit style="width: 80%">
              </el-input>
            </el-form-item>
            <el-divider></el-divider>
          </el-form-item>
        </el-form>
      </div>
      <el-button type="primary" style="float: right; margin-right: 15px; margin-bottom: 20px" @click="subAns">
        提交评价
      </el-button>
    </div>
    <!-- <div class="camera_outer">
      <div id="capOuter">
        <video
          id="capCamera"
          :width="videoWidth"
          :height="videoHeight"
          autoplay
        ></video>
        <div v-if="imgSrc" class="img_bg_camera" id="faceImg">
          <img :src="capImgSrc" alt="" class="tx_img" />
        </div>
      </div>
      <canvas
        style="display: none"
        id="capCanvasCamera"
        :width="videoWidth"
        :height="videoHeight"
      ></canvas>
    </div> -->
  </div>
</template>

<script>
import {
  PostExamContent,
  PostAnswerSetId,
  GetProblemPic,
  PostSimpleAns,
  PostGraphicAns,
} from "../request/api";
export default {
  name: "Exam",
  data() {
    return {
      title: "",
      duration: "",
      questions: [],
      ansList: [],
      fileList: [],
      answerSetId: -1,
      imgSrc: [],
      videoWidth: 300,
      videoHeight: 300,
      capImgSrc: "",
      thisCancas: null,
      thisContext: null,
      thisVideo: null,
      comments: ["优秀", "良好", "及格", "不合格"],
    };
  },
  computed: {},
  mounted() {
    this.title = this.$route.params.className + '-' + this.$route.params.problemSetName;
    // 获取当前考试ID对应的所有题目
    //获取当前试卷ID
    PostAnswerSetId({
      problemSetId: this.$route.params.problemSetId,
      classId: this.$route.params.classId,
    }).then((res) => {
      console.log(res);
      if (res.data.code == 0) {
        this.answerSetId = res.data.answerSetId;
        PostExamContent({
          problemSetId: this.$route.params.problemSetId,
        }).then((res) => {
          for (let i = 0; i < res.data.data.length; i++) {
            this.questions.push(res.data.data[i]);
            if (res.data.data[i].type == "单选题") {
              this.ansList.push({
                answerSetId: this.answerSetId,
                answer: -1,
                order: i,
              });
            } else {
              this.ansList.push({
                answerSetId: this.answerSetId,
                answer: "",
                order: i,
              });
            }
            GetProblemPic(res.data.data[i].problemId).then((res) => {
              this.createImgSrc(res.data, res.config.url.slice(9));
            });
          }
          console.log(this.imgSrc);
        });
      }
      else{
        this.$message("当前评教已过期");
      }
    });
  },
  methods: {
    goBack() {
      this.$router.push({ path: "/" });
    },
    subAns() {
      for (let i = 0; i < this.ansList.length; i++) {
        if (this.questions[i].type == "单选题") {
          PostSimpleAns(this.ansList[i]).then((res) => {
            console.log(res);
          });
        } else {
          PostGraphicAns(this.ansList[i]).then((res) => {
            console.log(res);
          });
        }
      }
      this.$router.push({ path: "/" });
      this.$$message("评教成功");
    },
    handleExceed() {
      this.$message.warning("超出文件上传的限制数量");
    },
    createImgSrc(blob, problemId) {
      // 浏览器允许使用URL.createObjectURL()方法，针对 Blob 对象生成一个临时 URL。
      // 这个 URL 以blob://开头,表明对应一个 Blob 对象。
      this.imgSrc.splice(0, 0);
      this.imgSrc[problemId] = window.URL.createObjectURL(blob);

    },
    uploadAnsImg(p) {
      this.ansList[p.data].append("image", p.file);
    },
  },
};
</script>

<style>
#examRoot {
  position: relative;
}

#examBox {
  position: absolute;
  border: 1px solid grey;
  left: 5%;
  width: 90%;
  top: 50px;
  user-select: none;
}

#examHeader {
  position: relative;
  width: 100%;
  height: 90px;
}

#examPageHeader {
  position: absolute;
  left: 10px;
  top: 20px;
}

#examDivider {
  position: absolute;
  top: 50px;
}

.examQuestionItem {
  margin-left: 15px;
}

.uploadAnsBtn {
  margin-left: 9px;
}

#capOuter {
  height: 310px;
  width: 310px;
  border: 1px solid #909399;
  position: absolute;
  /* top: 75px; */
  left: 90%;
  margin-left: -156px;
  border-radius: 10px;
  background-color: white;
}

#capCamera {
  position: relative;
  margin-top: 5px;
  margin-left: 5px;
  border-radius: 5px;
}
</style>