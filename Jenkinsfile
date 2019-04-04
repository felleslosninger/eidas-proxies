pipelineWithMavenAndDocker {
    verificationEnvironment = 'eid-verification2'
    stagingEnvironment = 'eid-staging'
    stagingEnvironmentType = 'puppet2'
    productionEnvironment = 'eid-production'
    puppetModules = 'eidas_sidp_proxy eidas_cidp_proxy'
    puppetApplyList = ['eid-systest-app01.dmz.local baseconfig,eidas_sidp_proxy,eidas_cidp_proxy']
}
