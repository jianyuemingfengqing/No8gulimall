<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="member_id" prop="userId">
      <el-input v-model="dataForm.userId" placeholder="member_id"></el-input>
    </el-form-item>
    <el-form-item label="收货人" prop="name">
      <el-input v-model="dataForm.name" placeholder="收货人"></el-input>
    </el-form-item>
    <el-form-item label="电话" prop="phone">
      <el-input v-model="dataForm.phone" placeholder="电话"></el-input>
    </el-form-item>
    <el-form-item label="右边" prop="postCode">
      <el-input v-model="dataForm.postCode" placeholder="右边"></el-input>
    </el-form-item>
    <el-form-item label="省份" prop="province">
      <el-input v-model="dataForm.province" placeholder="省份"></el-input>
    </el-form-item>
    <el-form-item label="城市" prop="city">
      <el-input v-model="dataForm.city" placeholder="城市"></el-input>
    </el-form-item>
    <el-form-item label="区" prop="region">
      <el-input v-model="dataForm.region" placeholder="区"></el-input>
    </el-form-item>
    <el-form-item label="详细地址" prop="address">
      <el-input v-model="dataForm.address" placeholder="详细地址"></el-input>
    </el-form-item>
    <el-form-item label="是否默认地址" prop="defaultStatus">
      <el-input v-model="dataForm.defaultStatus" placeholder="是否默认地址"></el-input>
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
          name: '',
          phone: '',
          postCode: '',
          province: '',
          city: '',
          region: '',
          address: '',
          defaultStatus: ''
        },
        dataRule: {
          userId: [
            { required: true, message: 'member_id不能为空', trigger: 'blur' }
          ],
          name: [
            { required: true, message: '收货人不能为空', trigger: 'blur' }
          ],
          phone: [
            { required: true, message: '电话不能为空', trigger: 'blur' }
          ],
          postCode: [
            { required: true, message: '右边不能为空', trigger: 'blur' }
          ],
          province: [
            { required: true, message: '省份不能为空', trigger: 'blur' }
          ],
          city: [
            { required: true, message: '城市不能为空', trigger: 'blur' }
          ],
          region: [
            { required: true, message: '区不能为空', trigger: 'blur' }
          ],
          address: [
            { required: true, message: '详细地址不能为空', trigger: 'blur' }
          ],
          defaultStatus: [
            { required: true, message: '是否默认地址不能为空', trigger: 'blur' }
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
              url: this.$http.adornUrl(`/ums/useraddress/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.userId = data.userAddress.userId
                this.dataForm.name = data.userAddress.name
                this.dataForm.phone = data.userAddress.phone
                this.dataForm.postCode = data.userAddress.postCode
                this.dataForm.province = data.userAddress.province
                this.dataForm.city = data.userAddress.city
                this.dataForm.region = data.userAddress.region
                this.dataForm.address = data.userAddress.address
                this.dataForm.defaultStatus = data.userAddress.defaultStatus
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
              url: this.$http.adornUrl(`/ums/useraddress/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'userId': this.dataForm.userId,
                'name': this.dataForm.name,
                'phone': this.dataForm.phone,
                'postCode': this.dataForm.postCode,
                'province': this.dataForm.province,
                'city': this.dataForm.city,
                'region': this.dataForm.region,
                'address': this.dataForm.address,
                'defaultStatus': this.dataForm.defaultStatus
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
