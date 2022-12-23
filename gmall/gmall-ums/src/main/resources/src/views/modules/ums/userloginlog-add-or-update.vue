<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="用户id" prop="userId">
      <el-input v-model="dataForm.userId" placeholder="用户id"></el-input>
    </el-form-item>
    <el-form-item label="登陆时间" prop="createTime">
      <el-input v-model="dataForm.createTime" placeholder="登陆时间"></el-input>
    </el-form-item>
    <el-form-item label="登录ip" prop="ip">
      <el-input v-model="dataForm.ip" placeholder="登录ip"></el-input>
    </el-form-item>
    <el-form-item label="登录城市" prop="city">
      <el-input v-model="dataForm.city" placeholder="登录城市"></el-input>
    </el-form-item>
    <el-form-item label="登录类型【0-web，1-移动】" prop="type">
      <el-input v-model="dataForm.type" placeholder="登录类型【0-web，1-移动】"></el-input>
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
          createTime: '',
          ip: '',
          city: '',
          type: ''
        },
        dataRule: {
          userId: [
            { required: true, message: '用户id不能为空', trigger: 'blur' }
          ],
          createTime: [
            { required: true, message: '登陆时间不能为空', trigger: 'blur' }
          ],
          ip: [
            { required: true, message: '登录ip不能为空', trigger: 'blur' }
          ],
          city: [
            { required: true, message: '登录城市不能为空', trigger: 'blur' }
          ],
          type: [
            { required: true, message: '登录类型【0-web，1-移动】不能为空', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/ums/userloginlog/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userId = data.userLoginLog.userId
                this.dataForm.createTime = data.userLoginLog.createTime
                this.dataForm.ip = data.userLoginLog.ip
                this.dataForm.city = data.userLoginLog.city
                this.dataForm.type = data.userLoginLog.type
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
              url: this.$http.adornUrl(`/ums/userloginlog/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'userId': this.dataForm.userId,
                'createTime': this.dataForm.createTime,
                'ip': this.dataForm.ip,
                'city': this.dataForm.city,
                'type': this.dataForm.type
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
