cloud demo
ssh-keygen -t rsa -C "454749293@qq.com" //ssh
git remote add origin git@github.com:ccw-han/cloud.git	 本地仓库关联远程
git push -u origin master		push到远程
上面出现的 [branch "master"]是需要明确(.git/config)如下的内容
[branch "master"]
remote = origin

merge = refs/heads/master

这等于告诉git2件事:

1，当你处于master branch, 默认的remote就是origin。

2，当你在master branch上使用git pull时，没有指定remote和branch，那么git就会采用默认的remote（也就是origin）来merge在master branch上所有的改变

如果不想或者不会编辑config文件的话，可以在bush上输入如下命令行：

$ git config branch.master.remote origin
$ git config branch.master.merge refs/heads/master

git pull origin master --allow-unrelated-histories
git remote rm origin
git config --global http.postBuffer 1048576000
