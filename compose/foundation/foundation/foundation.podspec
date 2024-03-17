Pod::Spec.new do |spec|
    spec.name                     = 'foundation'
    spec.version                  = '1.6.0-alpha06-topping01'
    spec.homepage                 = ''
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = ''
    spec.vendored_frameworks      = '../../../../../out/androidx/compose/foundation/foundation/build/cocoapods/framework/foundation.framework'
    spec.libraries                = 'c++'
                
                
                
    if !Dir.exist?('../../../../../out/androidx/compose/foundation/foundation/build/cocoapods/framework/foundation.framework') || Dir.empty?('../../../../../out/androidx/compose/foundation/foundation/build/cocoapods/framework/foundation.framework')
        raise "

        Kotlin framework 'foundation' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :compose:foundation:foundation:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':compose:foundation:foundation',
        'PRODUCT_MODULE_NAME' => 'foundation',
    }
                
    spec.script_phases = [
        {
            :name => 'Build foundation',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../../../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end