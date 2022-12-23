<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="用户id" prop="userId">
      <el-input v-model="dataForm.userId" placeholder="用户id"></el-input>
    </el-form-item>
    <el-form-item label="活动id" prop="subjectId">
      <el-input v-model="dataForm.subjectId" placeholder="活动id"></el-input>
    </el-form-item>
    <el-form-item label="活动名称" prop="subjectName">
      <el-input v-model="dataForm.subjectName" placeholder="活动名称"></el-input>
    </el-form-item>
    <el-form-item label="活动默认图片" prop="subjectImage">
      <el-input v-model="dataForm.subjectImage" placeholder="活动默认图片"></el-input>
    </el-form-item>
    <el-form-item label="活动链接" prop="subjectUrl">
      <el-input v-model="dataForm.subjectUrl" placeholder="活动链接"></el-input>
    </el-form-item>
    <el-form-item label="关注时间" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="关注时间"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          userId: '',
          subjectId: '',
          subjectName: '',
          subjectImage: '',
          subjectUrl: '',
          createTime: ''
        },
        dataRule: {
          userId: [
            { required: true, message: '用户id不能为空', trigger: 'blur' }
          ],
          subjectId: [
            { required: true, message: '活动id不能为空', trigger: 'blur' }
          ],
          subjectName: [
            { required: true, message: '活动名称不能为空', trigger: 'blur' }
          ],
          subjectImage: [
            { required: true, message: '活动默认图片不能为空', trigger: 'blur' }
          ],
          subjectUrl: [
            { required: true, message: '活动链接不能为空', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: '关注时间不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/ums/usercollectsubject/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userId = data.userCollectSubject.userId
                this.dataForm.subjectId = data.userCollectSubject.subjectId
                this.dataForm.subjectName = data.userCollectSubject.subjectName
                this.dataForm.subjectImage = data.userCollectSubject.subjectImage
                this.dataForm.subjectUrl = data.userCollectSubject.subjectUrl
                this.dataForm.createTime = data.userCollectSubject.createTime
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/ums/usercollectsubject/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'userId': this.dataForm.userId,
                'subjectId': this.dataForm.subjectId,
                'subjectName': this.dataForm.subjectName,
                'subjectImage': this.dataForm.subjectImage,
                'subjectUrl': this.dataForm.subjectUrl,
                'createTime': this.dataForm.createTime
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
