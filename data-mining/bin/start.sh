today=`date +%Y%m%d`
yesterday=`date +%Y%m%d -d "-1 days"`
amonthago=`date +%Y%m%d -d "-31 days"`
echo $today
echo $yesterday
echo $amonthago
conf="Location.xml"
if [ $1 ]; then
	conf=$1
fi
echo $conf
java -Xmx4g -server -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:logs/gc.log -cp "lib/*:target/classes" com.mobvoi.be.usercenter.datamining.UserCenterDataMiningJob -c $conf -s $amonthago -f $yesterday -d $today > stdout 2>stdout &
echo $! > job.pid
