#!/bin/bash
# build.sh

set -e

# Load SDKMAN
export SDKMAN_DIR="$HOME/.sdkman"

if [[ -s "$SDKMAN_DIR/bin/sdkman-init.sh" ]]; then
    source "$SDKMAN_DIR/bin/sdkman-init.sh"
else
    echo "SDKMAN not found"
    exit 1
fi

PROPERTIES_FILE="./gradle.properties"
GRADLE_WRAPPER_FILE="./gradle/wrapper/gradle-wrapper.properties"

# --------------------------
# FABRIC_MAP:
# mc_version => "fabric_api_version|mc_version_range|java_version|loom_version"
# --------------------------
declare -A FABRIC_MAP

# Common defaults
DEFAULT_JAVA="21"
DEFAULT_LOOM="1.11.7"

NEW_JAVA="25"
NEW_LOOM="1.15-SNAPSHOT"

DEFAULT_GRADLE="8.14"
NEW_GRADLE="9.4.1"

# Branch 1.14 -> 1.14.4
FABRIC_MAP["1.14"]="0.28.5+1.14|>=1.14 <= 1.14.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.14.1"]="0.28.5+1.14|>=1.14 <= 1.14.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.14.2"]="0.28.5+1.14|>=1.14 <= 1.14.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.14.3"]="0.28.5+1.14|>=1.14 <= 1.14.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.14.4"]="0.28.5+1.14|>=1.14 <= 1.14.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.15 -> 1.15.2
FABRIC_MAP["1.15"]="0.28.5+1.15|>=1.15 <=1.15.2|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.15.1"]="0.28.5+1.15|>=1.15 <=1.15.2|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.15.2"]="0.28.5+1.15|>=1.15 <=1.15.2|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.16 -> 1.16.5
FABRIC_MAP["1.16"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.16.1"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.16.2"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.16.3"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.16.4"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.16.5"]="0.42.0+1.16|>=1.16 <=1.16.5|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.17 -> 1.17.1
FABRIC_MAP["1.17"]="0.46.1+1.17|>=1.17 <=1.17.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.17.1"]="0.46.1+1.17|>=1.17 <=1.17.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.18
FABRIC_MAP["1.18"]="0.44.0+1.18|1.18|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.18.1
FABRIC_MAP["1.18.1"]="0.46.6+1.18|1.18.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.18.2
FABRIC_MAP["1.18.2"]="0.77.0+1.18.2|1.18.2|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.19
FABRIC_MAP["1.19"]="0.58.0+1.19|1.19|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.19.1
FABRIC_MAP["1.19.1"]="0.58.5+1.19.1|1.19.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.19.2
FABRIC_MAP["1.19.2"]="0.77.0+1.19.2|1.19.2|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.19.3
FABRIC_MAP["1.19.3"]="0.76.1+1.19.3|>=1.19.3|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.19.4
FABRIC_MAP["1.19.4"]="0.87.2+1.19.4|1.19.4|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.20
FABRIC_MAP["1.20"]="0.83.0+1.20|1.20|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.20.1 -> 1.21.10
FABRIC_MAP["1.20.1"]="0.92.8+1.20.1|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.20.2"]="0.91.6+1.20.2|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.20.3"]="0.91.1+1.20.3|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.20.4"]="0.97.3+1.20.4|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.20.5"]="0.97.8+1.20.5|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.20.6"]="0.100.8+1.20.6|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21"]="0.102.0+1.21|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.1"]="0.116.11+1.21.1|>=1.20.1 <=1.21.1|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Branch 1.21.2 -> 1.21.10
FABRIC_MAP["1.21.2"]="0.106.1+1.21.2|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.3"]="0.114.1+1.21.3|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.4"]="0.119.4+1.21.4|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.5"]="0.128.2+1.21.5|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.6"]="0.128.2+1.21.6|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.7"]="0.129.0+1.21.7|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.8"]="0.136.1+1.21.8|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.9"]="0.134.1+1.21.9|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"
FABRIC_MAP["1.21.10"]="0.138.4+1.21.10|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$DEFAULT_LOOM|$DEFAULT_GRADLE"

# Java 25 + Loom 1.16-SNAPSHOT
FABRIC_MAP["1.21.11"]="0.141.3+1.21.11|>=1.21.2 <=1.21.11|$DEFAULT_JAVA|$NEW_LOOM|$NEW_GRADLE"

# Branch 26.1 -> 26.1.2
FABRIC_MAP["26.1"]="0.145.1+26.1|>=26.1 <=26.1.2|$NEW_JAVA|$NEW_LOOM|$NEW_GRADLE"
FABRIC_MAP["26.1.1"]="0.145.4+26.1.1|>=26.1 <=26.1.2|$NEW_JAVA|$NEW_LOOM|$NEW_GRADLE"
FABRIC_MAP["26.1.2"]="0.148.0+26.1.2|>=26.1 <=26.1|$NEW_JAVA|$NEW_LOOM|$NEW_GRADLE"

# --------------------------
# Functions
# --------------------------

set_java_version() {
    local java_version="$1"

    if [[ "$java_version" == "21" ]]; then
        sdk use java 21.0.5-oracle
    elif [[ "$java_version" == "25" ]]; then
        sdk use java 25.0.2-tem
    else
        echo "Unsupported Java version: $java_version"
        exit 1
    fi
}

restore_java() {
    sdk use java 25.0.2-tem #IFS='|' read -r api_version mc_range java_version loom_version <<< "$val"IFS='|' read -r api_version mc_range java_version loom_version gradle_version <<< "$val"
}

replace_properties() {
    local mc="$1"
    local loader="$2"

    local val="${FABRIC_MAP[$mc]}"
    if [ -z "$val" ]; then
        echo "Error: No mapping found for Minecraft version $mc"
        exit 1
    fi

    IFS='|' read -r api_version mc_range java_version loom_version gradle_version <<< "$val"

    sed -i -E \
        "s#^distributionUrl=.*#distributionUrl=https\\\://services.gradle.org/distributions/gradle-${gradle_version}-bin.zip#" \
        "$GRADLE_WRAPPER_FILE"

    # Replace in gradle.properties
    sed -i -E "s/^minecraft_version=.*/minecraft_version=$mc/" "$PROPERTIES_FILE"
    sed -i -E "s/^fabric_api_version=.*/fabric_api_version=$api_version/" "$PROPERTIES_FILE"
    sed -i -E "s/^fabric_version=.*/fabric_version=$api_version/" "$PROPERTIES_FILE"
    sed -i -E "s/^minecraft_version_range=.*/minecraft_version_range=$mc_range/" "$PROPERTIES_FILE"
    sed -i -E "s/^loom_version=.*/loom_version=$loom_version/" "$PROPERTIES_FILE"

    echo "Updated gradle.properties for MC $mc, loader $loader"
    echo "Java: $java_version"
    echo "Minecraft: $mc"
    echo "Fabric API: $api_version"
    echo "Loom: $loom_version"
    echo "Gradle: $gradle_version"

    CURRENT_JAVA_VERSION="$java_version"
}

version_matches() {
    local version="$1"
    local filter="$2"

    if [[ -z "$filter" ]]; then
        return 0
    fi

    # exact match
    [[ "$filter" == "$version" ]] && return 0

    # range e.g. 1.20-1.21
    if [[ "$filter" =~ ^([0-9]+\.[0-9]+)-([0-9]+\.[0-9]+)$ ]]; then
        local start="${BASH_REMATCH[1]}"
        local end="${BASH_REMATCH[2]}"
        [[ "$(printf '%s\n%s\n' "$start" "$version" | sort -V | head -n1)" == "$start" ]] &&
        [[ "$(printf '%s\n%s\n' "$version" "$end" | sort -V | head -n1)" == "$version" ]] && return 0
    fi

    # inequalities e.g. >1.20, >=1.20
    if [[ "$filter" =~ ^([><]=?)([0-9]+\.[0-9]+)$ ]]; then
        local op="${BASH_REMATCH[1]}"
        local v="${BASH_REMATCH[2]}"
        case "$op" in
            ">") [[ "$(printf '%s\n%s\n' "$v" "$version" | sort -V | head -n1)" != "$v" ]] && return 0 ;;
            ">=") [[ "$(printf '%s\n%s\n' "$v" "$version" | sort -V | head -n1)" == "$v" ]] && return 0 ;;
        esac
    fi

    return 1
}

# Always restore Java 25 when exiting
trap restore_java EXIT

# --------------------------
# Main
# --------------------------

LOADER="$1"
MCVERSION="$2"
GRADLEMODE="$3"

if [[ "$LOADER" == "test" ]]; then
    # test mode: iterate all versions
    for mc in "${!FABRIC_MAP[@]}"; do
        version_matches "$mc" "$MCVERSION" || continue

        replace_properties "$mc" "fabric"

        set_java_version "$CURRENT_JAVA_VERSION"

        ./gradlew :fabric:"$GRADLEMODE"
    done
else
    # single version build
    replace_properties "$MCVERSION" "$LOADER"

    set_java_version "$CURRENT_JAVA_VERSION"

    ./gradlew :"$LOADER":"$GRADLEMODE"
fi