#!/bin/bash

# Script to check and update pom.xml with required JaCoCo and SonarQube plugins

POM_FILE="pom.xml"
BACKUP_FILE="pom.xml.backup"

# Function to check if a plugin exists in pom.xml
check_plugin_exists() {
    local group_id=$1
    local artifact_id=$2
    grep -q "<groupId>$group_id</groupId>" "$POM_FILE" && \
    grep -A 5 "<groupId>$group_id</groupId>" "$POM_FILE" | grep -q "<artifactId>$artifact_id</artifactId>"
}

# Function to add properties if they don't exist
add_properties() {
    if ! grep -q "<jacoco.version>" "$POM_FILE"; then
        echo "Adding JaCoCo version property..."
        # Find the properties section and add jacoco version
        sed -i '/<properties>/a\        <jacoco.version>0.8.12</jacoco.version>' "$POM_FILE"
    fi

    if ! grep -q "<sonar.version>" "$POM_FILE"; then
        echo "Adding SonarQube version property..."
        # Find the properties section and add sonar version
        sed -i '/<properties>/a\        <sonar.version>4.0.0.4121</sonar.version>' "$POM_FILE"
    fi

    if ! grep -q "<maven.surefire.version>" "$POM_FILE"; then
        echo "Adding Maven Surefire version property..."
        sed -i '/<properties>/a\        <maven.surefire.version>3.3.1</maven.surefire.version>' "$POM_FILE"
    fi

    if ! grep -q "<maven.site.version>" "$POM_FILE"; then
        echo "Adding Maven Site version property..."
        sed -i '/<properties>/a\        <maven.site.version>4.0.0-M16</maven.site.version>' "$POM_FILE"
    fi
}

# Function to add JaCoCo plugin
add_jacoco_plugin() {
    echo "Adding JaCoCo plugin to pom.xml..."
    cat << 'EOF' > temp_jacoco.xml

            <!-- JaCoCo Plugin for Code Coverage -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
EOF

    # Insert the plugin before the closing </plugins> tag
    sed -i '/<\/plugins>/e cat temp_jacoco.xml' "$POM_FILE"
    rm temp_jacoco.xml
}

# Function to add SonarQube plugin
add_sonar_plugin() {
    echo "Adding SonarQube plugin to pom.xml..."
    cat << 'EOF' > temp_sonar.xml

            <!-- SonarQube Plugin -->
            <plugin>
                <groupId>org.sonarsource.scanner.maven</groupId>
                <artifactId>sonar-maven-plugin</artifactId>
                <version>${sonar.version}</version>
            </plugin>
EOF

    # Insert the plugin before the closing </plugins> tag
    sed -i '/<\/plugins>/e cat temp_sonar.xml' "$POM_FILE"
    rm temp_sonar.xml
}

# Function to add Maven Surefire plugin if not present
add_surefire_plugin() {
    if ! check_plugin_exists "org.apache.maven.plugins" "maven-surefire-plugin"; then
        echo "Adding Maven Surefire plugin to pom.xml..."
        cat << 'EOF' > temp_surefire.xml

            <!-- Maven Surefire Plugin for Test Reports -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*Tests.java</include>
                    </includes>
                </configuration>
            </plugin>
EOF

        sed -i '/<\/plugins>/e cat temp_surefire.xml' "$POM_FILE"
        rm temp_surefire.xml
    fi
}

# Function to add Maven Site plugin if not present
add_site_plugin() {
    if ! check_plugin_exists "org.apache.maven.plugins" "maven-site-plugin"; then
        echo "Adding Maven Site plugin to pom.xml..."
        cat << 'EOF' > temp_site.xml

            <!-- Maven Site Plugin for Reports -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-site-plugin</artifactId>
                <version>4.0.0-M16</version>
            </plugin>
EOF

        sed -i '/<\/plugins>/e cat temp_site.xml' "$POM_FILE"
        rm temp_site.xml
    fi
}

# Main script execution
echo "Checking pom.xml for required plugins..."

if [ ! -f "$POM_FILE" ]; then
    echo "Error: pom.xml not found in current directory"
    exit 1
fi

# Create backup
cp "$POM_FILE" "$BACKUP_FILE"
echo "Created backup: $BACKUP_FILE"

# Track if any changes were made
changes_made=false

# Add properties if missing
if ! grep -q "<jacoco.version>" "$POM_FILE" || ! grep -q "<sonar.version>" "$POM_FILE"; then
    add_properties
    changes_made=true
fi

# Check and add JaCoCo plugin
if ! check_plugin_exists "org.jacoco" "jacoco-maven-plugin"; then
    echo "JaCoCo plugin not found, adding it..."
    add_jacoco_plugin
    changes_made=true
else
    echo "JaCoCo plugin already exists"
fi

# Check and add SonarQube plugin
if ! check_plugin_exists "org.sonarsource.scanner.maven" "sonar-maven-plugin"; then
    echo "SonarQube plugin not found, adding it..."
    add_sonar_plugin
    changes_made=true
else
    echo "SonarQube plugin already exists"
fi

# Add additional useful plugins
add_surefire_plugin
add_site_plugin

if [ "$changes_made" = true ]; then
    echo "✅ pom.xml has been updated with required plugins"
    echo "📋 Changes made:"
    echo "   - Added JaCoCo plugin for code coverage"
    echo "   - Added SonarQube plugin for static analysis"
    echo "   - Added Maven Surefire plugin for test reports"
    echo "   - Added Maven Site plugin for report generation"
    echo ""
    echo "💾 Backup saved as: $BACKUP_FILE"
else
    echo "✅ All required plugins are already present in pom.xml"
    rm "$BACKUP_FILE"  # Remove backup if no changes were made
fi

# Validate the updated pom.xml
echo "Validating pom.xml..."
if mvn validate -q; then
    echo "✅ pom.xml is valid"
else
    echo "❌ pom.xml validation failed, restoring backup..."
    mv "$BACKUP_FILE" "$POM_FILE"
    exit 1
fi

echo "Script completed successfully!"
