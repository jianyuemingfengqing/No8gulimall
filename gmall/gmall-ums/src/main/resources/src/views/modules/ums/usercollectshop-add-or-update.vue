<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="用户id" prop="userId">
      <el-input v-model="dataForm.userId" placeholder="用户id"></el-input>
    </el-form-item>
    <el-form-item label="店铺id" prop="shopId">
      <el-input v-model="dataForm.shopId" placeholder="店铺id"></el-input>
    </el-form-item>
    <el-form-item label="店铺名" prop="shopName">
      <el-input v-model="dataForm.shopName" placeholder="店铺名"></el-input>
    </el-form-item>
    <el-form-item label="店铺logo" prop="shopLogo">
      <el-input v-model="dataForm.shopLogo" placeholder="店铺logo"></el-input>
    </el-form-item>
    <el-form-item label="关注时间" prop="createtime">
      <el-input v-model="dataForm.createtime" placeholder="关注时间"></el-input>
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
          shopId: '',
          shopName: '',
          shopLogo: '',
          createtime: ''
        },
        dataRule: {
          userId: [
            { required: true, message: '用户id不能为空', trigger: 'blur' }
          ],
          shopId: [
            { required: true, message: '店铺id不能为空', trigger: 'blur' }
          ],
          shopName: [
            { required: true, message: '店铺名不能为空', trigger: 'blur' }
          ],
          shopLogo: [
            { required: true, message: '店铺logo不能为空', trigger: 'blur' }
          ],
          createtime: [
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
              url: this.$http.adornUrl(`/ums/usercollectshop/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userId = data.userCollectShop.userId
                this.dataForm.shopId = data.userCollectShop.shopId
                this.dataForm.shopName = data.userCollectShop.shopName
                this.dataForm.shopLogo = data.userCollectShop.shopLogo
                this.dataForm.createtime = data.userCollectShop.createtime
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
              url: this.$http.adornUrl(`/ums/usercollectshop/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'userId': this.dataForm.userId,
                'shopId': this.dataForm.shopId,
                'shopName': this.dataForm.shopName,
                'shopLogo': this.dataForm.shopLogo,
                'createtime': this.dataForm.createtime
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
