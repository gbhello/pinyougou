//显示买家名称服务层
app.service('indexService',function($http){
	this.loginName=function(){
		return $http.get('../login/name.do')
	}
});