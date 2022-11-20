// 导入request.js
import request from './request';



// 按需导出每个API
// 登陆
export const Login = (params) => request.post('/user/login', params);

// 注册
export const Register = (p) => request.post('/user/register', p);

// 获取用户信息
export const GetUserInfo = () => request.get('/user');

// 创建学生
export const PostStudent = (p) => request.post('/user/student', p);

// 获取创建的班级
export const GetCourseCreated = () => request.get('/course/create');

// 创建班级
export const PostCourse = (p) => request.post('/course/create', p);

// 获取自己加入的班级
export const GetCourseJoined = () => request.get('/course/join');

// 分配班级给学生
export const PostCourseJoined = (p) => request.post('/course/join', p);

// 获取全班学生ID
export const GetCourseStu = (str) => request.get('/course/' + str);

// 上传题目
export const PostProblem = (p) => request.post('/problem', p);

// 获取题目ID
export const GetProblemId = (str) => request.get('/problem/' + str);

// 获取题目ID对应图片
export const GetProblemPic = (str) => request.get('/problem/' + str, {
    responseType: 'blob'
});

// 获取所有的评教
export const GetExamJoined = (p) => request.post('/exam/info', p);

// 提交评教名称
export const PostProblemSet = (p) => request.post('/exam', p);

// 修改评教内容
export const PutProblemSet = (p) => request.put('/exam', p);

// 获取创建的评教
export const GetExamCreated = () => request.get('/exam');

// 发布评教到班级
export const PostExam = (p) => request.post('/exam/publish', p);

// 获取评教题目
export const PostExamContent = (p) => request.post('/exam/content', p);

// 获取评教卷子ID
export const PostAnswerSetId = (p) => request.post('/answer', p);

// 提交选择题答案
export const PostSimpleAns = (p) => request.post('/answer/simple', p);

// 提交填空题答案
export const PostGraphicAns = (p) => request.post('/answer/graphic', p);

// 获取选择题答案
export const PostSimpleAnsGet = (str) => request.post('/answer/simple/'+str);

// 获取填空题答案图片
export const GetAnsPic = (str) => request.get('/answer/graphic/' + str);

// 获取全班答题卡
export const PostAnsList = (p) => request.post('/answer/list', p);

// 获取词云
export const GetCloudPic = (str) => request.get('/answer/cloud/'+str, {
    responseType: 'blob'
});

// 获取情感分布
export const PostEmotion = (p) => request.post('/answer/emotion',p);

// 获取单选频率分布
export const PostChoice = (p)=> request.post('/answer/choice',p);

// 新建机构
export const PostInstitute = (p) => request.post('/institute', p);

// 搜索班级
export const PostCourseSearch = (str, p) => request.post('/course' + str, p);

