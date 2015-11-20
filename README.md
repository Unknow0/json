json
====

A java implementation of Json.

How to use in maven
===========

For lastest release version:

	<project>
		<repositories>
			<repository>
				<id>unknow</id>
				<url>http://unknow.hd.free.fr/nexus/content/repositories/release/</url>
				<snapshots>
					<enabled>false</enabled>
				</snapshots>
			</repository>
		</repositories>

		<dependencies>
			<dependency>
				<groupId>unknow</groupId>
				<artifactId>json</artifactId>
				<version>1.1</version>
			</dependency>
		</dependencies>
	</project>


For the developement version:

	<project>
		<repositories>
			<repository>
				<id>unknow</id>
				<url>http://unknow.hd.free.fr/nexus/content/repositories/snapshots/</url>
				<snapshots>
					<enabled>true</enabled>
				</snapshots>
			</repository>
		</repositories>

		<dependencies>
			<dependency>
				<groupId>unknow</groupId>
				<artifactId>json</artifactId>
				<version>1.2-SNAPSHOT</version>
			</dependency>
		</dependencies>
	</project>
